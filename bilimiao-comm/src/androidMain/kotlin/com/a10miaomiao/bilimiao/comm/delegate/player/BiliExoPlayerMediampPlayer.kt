package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.Context
import androidx.media3.common.MediaItem
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

/**
 * B站 ExoPlayer 包装器
 *
 * 参考 animeko `LibassExoPlayerMediampPlayer` 的设计：
 * 包装 [ExoPlayerMediampPlayer]，在 mediamp 框架内正确处理 B站 DASH 音视频分离流。
 *
 * mediamp 的 `setMediaData(UriMediaData(videoUrl))` 只能设置单流（视频），
 * 无法注入外部音频流。本类覆盖 `resume`：
 * - 先调 `exoMediampPlayer.resume()` 触发 READY→PLAYING 状态转换，
 *   然后立即用 `exoPlayer.setMediaSource(mergingSource)` 覆盖为合并源。
 *
 * 使用方式：
 * 1. `setPendingMediaSource(mergingSource)` 设置待应用的合并源
 * 2. `setMediaData(UriMediaData(videoUrl, headers))` 设置视频流（状态→READY）
 * 3. `resume()` 时自动应用 pendingMediaSource
 *
 * @param context Android Context
 * @param parentCoroutineContext 协程上下文
 */
@OptIn(org.openani.mediamp.InternalForInheritanceMediampApi::class)
class BiliExoPlayerMediampPlayer private constructor(
    private val delegate: ExoPlayerMediampPlayer,
    context: Context,
) : MediampPlayer by delegate {

    private val appContext = context.applicationContext

    companion object {
        operator fun invoke(
            context: Context,
            parentCoroutineContext: kotlin.coroutines.CoroutineContext,
        ): BiliExoPlayerMediampPlayer {
            val delegate = ExoPlayerMediampPlayer(context, parentCoroutineContext)
            return BiliExoPlayerMediampPlayer(delegate, context)
        }
    }

    /** 底层 ExoPlayerMediampPlayer 委托实例，供 ExoPlayerMediampPlayerSurface 使用 */
    val mediampDelegate: ExoPlayerMediampPlayer get() = delegate

    /** 底层 ExoPlayer，用于直接操作 MediaSource */
    internal val exoPlayer: ExoPlayer get() = delegate.impl

    /** 待应用的合并媒体源（含视频+音频），resume 时覆盖 */
    private var pendingMediaSource: MediaSource? = null

    /**
     * 设置待应用的合并媒体源
     *
     * 在 `setMediaData` 之前调用，设置包含视频和音频的 [MergingMediaSource]。
     * 当 `resume()` 被调用时，此源会覆盖 mediamp 默认设置的单流 MediaSource。
     */
    fun setPendingMediaSource(
        videoUrl: String,
        audioUrl: String,
        headers: Map<String, String>,
    ) {
        pendingMediaSource = createMergingMediaSource(videoUrl, audioUrl, headers)
    }

    /**
     * 恢复播放
     *
     * 参考 animeko：先调 mediamp 的 resume（触发状态转换），
     * 然后立即用合并源覆盖 ExoPlayer 的 MediaSource。
     */
    override fun resume() {
        val mediaSource = pendingMediaSource
        if (mediaSource == null || delegate.getCurrentPlaybackState() != PlaybackState.READY) {
            delegate.resume()
            return
        }

        pendingMediaSource = null
        // 先让 mediamp 完成 READY→PLAYING 状态转换
        delegate.resume()
        // 然后立即用合并源覆盖（包含视频+音频）
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    override fun stopPlayback() {
        pendingMediaSource = null
        delegate.stopPlayback()
    }

    override fun seekTo(positionMillis: Long) {
        if (delegate.getCurrentPlaybackState() < PlaybackState.READY) return
        delegate.seekTo(positionMillis)
    }

    override fun close() {
        pendingMediaSource = null
        delegate.close()
    }

    /**
     * 创建音视频合并的 MediaSource
     *
     * 使用 [DefaultMediaSourceFactory] 自动选择合适的 extractor（FragmentedMp4 等），
     * 能正确解析 B站 DASH 的 .m4s 格式。
     */
    private fun createMergingMediaSource(
        videoUrl: String,
        audioUrl: String,
        headers: Map<String, String>,
    ): MediaSource {
        val dataSourceFactory: DataSource.Factory = if (videoUrl.startsWith("file://")) {
            DefaultDataSource.Factory(appContext)
        } else {
            val userAgent = headers["User-Agent"] ?: "Bilibili Freedoooooom/MarkII"
            DefaultHttpDataSource.Factory()
                .setUserAgent(userAgent)
                .setDefaultRequestProperties(headers)
        }

        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        val videoMedia = MediaItem.Builder().setUri(videoUrl).build()
        val audioMedia = MediaItem.Builder().setUri(audioUrl).build()
        val videoSource = mediaSourceFactory.createMediaSource(videoMedia)
        val audioSource = mediaSourceFactory.createMediaSource(audioMedia)
        return MergingMediaSource(videoSource, audioSource)
    }
}
