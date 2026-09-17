package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.Context
import org.openani.mediamp.MediampPlayer
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders
import com.a10miaomiao.bilimiao.comm.platform.AndroidPlatformContext

/**
 * 安卓端 actual：创建 BiliExoPlayerMediampPlayer
 *
 * 使用自定义包装器，支持 B站 DASH 音视频分离流：包装器通过 mediamp 的
 * `mediaSourceInterceptor` 在每次 open 时用 [androidx.media3.exoplayer.source.MergingMediaSource]
 * 合并视频流与外部音频流。
 */
actual fun createMediampPlayer(): MediampPlayer {
    val context = PlatformProviders.context
    val androidContext = (context as AndroidPlatformContext).platformContext as Context
    return BiliExoPlayerMediampPlayer(androidContext, kotlinx.coroutines.Dispatchers.Main)
}

/**
 * 安卓端 actual：声明外部音频轨（B站 DASH 音视频分离）
 *
 * 只做声明，真正的接入由 [BiliExoPlayerMediampPlayer] 在 ExoPlayer open 时以
 * [androidx.media3.exoplayer.source.MergingMediaSource] 合并（视频 + 音频一次 open）。
 */
actual fun setExternalAudioTrack(
    player: MediampPlayer,
    videoUrl: String,
    audioUrl: String?,
    headers: Map<String, String>,
) {
    val biliPlayer = player as? BiliExoPlayerMediampPlayer ?: return
    biliPlayer.setExternalAudioTrack(videoUrl, audioUrl, headers)
}

/**
 * 安卓端 actual：把标题 / UP主 / 封面写入底层 ExoPlayer 的 MediaItem 元数据
 *
 * 由 PlaybackService 的 MediaSession 渲染成通知栏播放器控制器的标题与封面。
 */
actual fun updateMediaSessionMetadata(
    player: MediampPlayer,
    title: String?,
    artist: String?,
    artworkUri: String?,
) {
    val biliPlayer = player as? BiliExoPlayerMediampPlayer ?: return
    biliPlayer.setNotificationMetadata(title, artist, artworkUri)
}

/**
 * 安卓端 actual：通过 ExoPlayer.setVolume 设置音量 (0-100 → 0.0-1.0)
 *
 * ExoPlayerMediampPlayer 不支持 AudioLevelController feature，
 * 直接操作底层 ExoPlayer 的 volume 属性。
 */
actual fun setPlayerVolume(player: MediampPlayer, volume: Int) {
    try {
        val exoPlayer = (player as BiliExoPlayerMediampPlayer).exoPlayer
        // ExoPlayer volume 范围 0.0-1.0
        exoPlayer.volume = (volume / 100f).coerceIn(0f, 1f)
    } catch (_: Exception) {}
}

/**
 * 安卓端 actual：应用「占用音频焦点」开关
 *
 * 交由 media3 的 `handleAudioFocus` 处理：开启时播放期间申请音频焦点
 * （被其它应用抢占时暂停、释放后自动续播），关闭时不申请音频焦点。
 */
actual fun setPlayerAudioFocusEnabled(player: MediampPlayer, enabled: Boolean) {
    val biliPlayer = player as? BiliExoPlayerMediampPlayer ?: return
    biliPlayer.setAudioFocusEnabled(enabled)
}

/**
 * 安卓端 actual：通过 Activity.requestedOrientation 设置屏幕方向
 *
 * Activity 引用由 [activityHolder] 提供（在 MainActivity 中初始化）。
 */
actual fun setRequestedOrientation(request: ScreenOrientationRequest) {
    try {
        val activity = ActivityHolder.get() ?: return
        val orientation = when (request) {
            ScreenOrientationRequest.UNSPECIFIED -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            ScreenOrientationRequest.PORTRAIT -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            ScreenOrientationRequest.SENSOR_LANDSCAPE -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            ScreenOrientationRequest.LANDSCAPE -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            ScreenOrientationRequest.REVERSE_LANDSCAPE -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        }
        activity.requestedOrientation = orientation
    } catch (_: Exception) {}
}

/** Activity 引用持有器（WeakReference，由 MainActivity 在 onCreate 中设置） */
object ActivityHolder {
    private var ref: java.lang.ref.WeakReference<android.app.Activity>? = null

    fun set(activity: android.app.Activity) {
        ref = java.lang.ref.WeakReference(activity)
    }

    fun get(): android.app.Activity? = ref?.get()
}
