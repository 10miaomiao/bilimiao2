package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.PlaybackState
import org.openani.mediamp.exoplayer.ExoPlayerMediampPlayer
import org.openani.mediamp.source.MediaData
import org.openani.mediamp.source.UriMediaData

/**
 * B站 ExoPlayer 包装器
 *
 * mediamp 的 `setMediaData(UriMediaData(videoUrl))` 只能设置单流（视频），无法表达
 * B站 DASH 音视频分离。本类通过 mediamp 的 `mediaSourceInterceptor` 钩子在每次
 * open 时把独立音频流合并进即将加载的媒体源（[MergingMediaSource]）：
 * 视频与音频在同一个 open 内一起下发，不存在「先加载视频、再补音频」的时机竞态。
 *
 * 使用方式：
 * 1. `setExternalAudioTrack(videoUrl, audioUrl)` 声明即将加载的媒体的外部音频
 *    （audioUrl 为 null 表示无独立音频，清除上一次的声明）
 * 2. `setMediaData(UriMediaData(videoUrl, headers))` 加载视频流，音频在 open 时接入
 *
 * 注意：不要改回「先单流 open，再在 resume 时覆盖 MediaSource」的写法。
 * 那依赖 open 完成瞬间的播放状态（mediamp 已废弃 v1 状态枚举的顺序约定），
 * 状态不满足时会静默丢掉音频（无声），残留的媒体源还会在后续 resume 时
 * 把旧媒体错配给当前视频。
 *
 * @param context Android Context
 * @param parentCoroutineContext 协程上下文
 */
@OptIn(org.openani.mediamp.InternalForInheritanceMediampApi::class)
class BiliExoPlayerMediampPlayer private constructor(
    private val delegate: ExoPlayerMediampPlayer,
    private val externalAudio: ExternalAudioInterceptor,
    private val notificationMetadata: NotificationMetadataInterceptor,
) : MediampPlayer by delegate {

    companion object {
        operator fun invoke(
            context: Context,
            parentCoroutineContext: kotlin.coroutines.CoroutineContext,
        ): BiliExoPlayerMediampPlayer {
            val audioInterceptor = ExternalAudioInterceptor(context.applicationContext)
            val metadataInterceptor = NotificationMetadataInterceptor()
            val delegate = ExoPlayerMediampPlayer(
                context = context,
                parentCoroutineContext = parentCoroutineContext,
                mediaSourceInterceptor = { source, data ->
                    // 先接入外部音频，再附加通知栏元数据（顺序无关，但合并后统一附加更直观）
                    metadataInterceptor.intercept(audioInterceptor.intercept(source, data))
                },
            )
            return BiliExoPlayerMediampPlayer(delegate, audioInterceptor, metadataInterceptor)
        }
    }

    init {
        // 交给 PlaybackService 的 MediaSession，由 media3 在播放时生成通知栏播放器控制器
        MediaSessionBridge.registerPlayer(exoPlayer)
    }

    /** 底层 ExoPlayerMediampPlayer 委托实例，供 ExoPlayerMediampPlayerSurface 使用 */
    val mediampDelegate: ExoPlayerMediampPlayer get() = delegate

    /** 底层 ExoPlayer，用于直接操作 MediaSource */
    internal val exoPlayer: ExoPlayer get() = delegate.impl

    /**
     * 声明接下来要加载的媒体的外部音频轨（B站 DASH 音视频分离）
     *
     * 必须在 `setMediaData` 之前调用；[audioUrl] 为 null 或空白表示该媒体没有独立音频，
     * 此时会清除上一次的声明，避免音频错配到当前或下一个视频。
     */
    fun setExternalAudioTrack(
        videoUrl: String,
        audioUrl: String?,
        headers: Map<String, String>,
    ) {
        externalAudio.set(videoUrl, audioUrl, headers)
    }

    override fun stopPlayback() {
        // 停止播放时丢弃未消费的音频声明，避免应用到之后无关的媒体
        externalAudio.clear()
        delegate.stopPlayback()
    }

    override fun seekTo(positionMillis: Long) {
        if (delegate.getCurrentPlaybackState() < PlaybackState.READY) return
        delegate.seekTo(positionMillis)
    }

    /**
     * 应用「占用音频焦点」开关（对齐旧版 DanmakuVideoPlayer.enabledAudioFocus）
     *
     * 通过 media3 的 `handleAudioFocus` 控制音频焦点：
     * - 开启：播放期间申请音频焦点，其它应用播放 / 来电抢占时自动暂停，焦点恢复后自动续播；
     * - 关闭：不申请音频焦点，可与其它应用同时出声。
     *
     * media3 支持运行时切换（`setAudioAttributes` 会据此立即申请 / 放弃焦点），
     * 因此设置变更无需重建播放器。
     */
    fun setAudioFocusEnabled(enabled: Boolean) {
        exoPlayer.setAudioAttributes(MEDIA_AUDIO_ATTRIBUTES, enabled)
    }

    /**
     * 更新通知栏播放器控制器展示的信息（标题 / UP主 / 封面）
     *
     * 必须在加载媒体（`setMediaData`）之前调用，元数据会随本次 open 写入 MediaItem，
     * 否则通知栏只显示播放地址。
     */
    fun setNotificationMetadata(title: String?, artist: String?, artworkUri: String?) {
        notificationMetadata.set(title, artist, artworkUri)
    }

    override fun close() {
        externalAudio.clear()
        notificationMetadata.clear()
        MediaSessionBridge.unregisterPlayer(exoPlayer)
        delegate.close()
    }
}

/**
 * 外部音频（DASH 音视频分离）接入拦截器
 *
 * mediamp 在每次 open 时回调 `mediaSourceInterceptor`（视频 MediaSource 已构建、
 * 尚未交给播放器），此时把音频源合并进去即可让视频与音频在同一次 open 中生效。
 */
private class ExternalAudioInterceptor(
    private val appContext: Context,
) {

    private data class Spec(
        val videoUrl: String,
        val audioUrl: String,
        val headers: Map<String, String>,
    )

    /** 待接入的音频声明（由加载协程写入，open 时在播放器线程消费） */
    @Volatile
    private var spec: Spec? = null

    fun set(videoUrl: String, audioUrl: String?, headers: Map<String, String>) {
        spec = if (audioUrl.isNullOrBlank()) null else Spec(videoUrl, audioUrl, headers)
    }

    fun clear() {
        spec = null
    }

    /**
     * 仅对接入地址匹配的媒体合并音频源
     *
     * 快速切换视频时，旧协程的 open 可能晚于新声明到达；按地址匹配可以避免把
     * 新视频的音频错配给旧视频（此时旧 open 保持无音频，随即被新视频取代）。
     */
    fun intercept(videoSource: MediaSource, data: MediaData): MediaSource {
        val current = spec ?: return videoSource
        if ((data as? UriMediaData)?.uri != current.videoUrl) return videoSource
        // 已消费，避免影响后续其它媒体的 open
        spec = null
        return MergingMediaSource(videoSource, createAudioSource(current))
    }

    private fun createAudioSource(spec: Spec): MediaSource {
        val dataSourceFactory: DataSource.Factory = if (spec.audioUrl.startsWith("file://")) {
            // 本地下载的分离音频（[local-merging]）
            DefaultDataSource.Factory(appContext)
        } else {
            DefaultHttpDataSource.Factory()
                .setUserAgent(spec.headers["User-Agent"] ?: DEFAULT_USER_AGENT)
                .setDefaultRequestProperties(spec.headers)
        }
        val audioMedia = MediaItem.Builder().setUri(spec.audioUrl).build()
        return DefaultMediaSourceFactory(dataSourceFactory).createMediaSource(audioMedia)
    }
}

/**
 * 通知栏元数据接入拦截器
 *
 * 通知栏播放器控制器的标题 / UP主 / 封面取自播放器当前 MediaItem 的 [MediaMetadata]，
 * 而 mediamp 构建 MediaItem 时只设置 uri，因此这里在每次 open 时把业务侧
 * （[com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegateImpl]）设置好的
 * 元数据写入即将加载的媒体源。
 */
@androidx.annotation.OptIn(markerClass = [UnstableApi::class])
private class NotificationMetadataInterceptor {

    @Volatile
    private var mediaMetadata: MediaMetadata? = null

    fun set(title: String?, artist: String?, artworkUri: String?) {
        mediaMetadata = if (title.isNullOrBlank() && artist.isNullOrBlank() && artworkUri.isNullOrBlank()) {
            null
        } else {
            MediaMetadata.Builder().apply {
                title?.let { setTitle(it) }
                artist?.let { setArtist(it) }
                artworkUri?.let { setArtworkUri(Uri.parse(it)) }
            }.build()
        }
    }

    fun clear() {
        mediaMetadata = null
    }

    fun intercept(source: MediaSource): MediaSource {
        val metadata = mediaMetadata ?: return source
        // updateMediaItem 是「整体替换」语义，必须以原 MediaItem 为基础派生：
        // 若传入只带 mediaMetadata 的 MediaItem，localConfiguration（uri）会被置空，
        // 播放时 ProgressiveMediaSource.getLocalConfiguration() 的 checkNotNull 会抛 NPE。
        val updatedItem = source.mediaItem.buildUpon().setMediaMetadata(metadata).build()
        if (!source.canUpdateMediaItem(updatedItem)) return source
        source.updateMediaItem(updatedItem)
        return source
    }
}

private const val DEFAULT_USER_AGENT = "Bilibili Freedoooooom/MarkII"

/**
 * 播放媒体的音频属性（视频场景）。
 *
 * media3 要求开启音频焦点处理时 usage 为 [C.USAGE_MEDIA] 或 [C.USAGE_GAME]，
 * 内容类型声明为影片，使系统 / 蓝牙设备按媒体播放处理，并在抢占音频焦点时按
 * 「媒体」类别的惯例暂停本应用。
 */
private val MEDIA_AUDIO_ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
    .setUsage(C.USAGE_MEDIA)
    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
    .build()
