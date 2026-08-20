package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.Context
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.source.UriMediaData
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders
import com.a10miaomiao.bilimiao.comm.platform.AndroidPlatformContext

/**
 * 安卓端 actual：创建 BiliExoPlayerMediampPlayer
 *
 * 使用自定义包装器，支持 B站 DASH 音视频分离流（MergingMediaSource）。
 * 参考 animeko LibassExoPlayerMediampPlayer 的 setMediaData/resume 覆盖方案。
 */
actual fun createMediampPlayer(): MediampPlayer {
    val context = PlatformProviders.context
    val androidContext = (context as AndroidPlatformContext).platformContext as Context
    return BiliExoPlayerMediampPlayer(androidContext, kotlinx.coroutines.Dispatchers.Main)
}

/**
 * 安卓端 actual：通过 BiliExoPlayerMediampPlayer 设置音视频分离的媒体数据
 *
 * 创建 [MergingMediaData] 传入 [BiliExoPlayerMediampPlayer.setMediaData]，
 * 由包装器在 resume 时用 MergingMediaSource 覆盖 ExoPlayer 的媒体源。
 */
actual suspend fun setMergingMediaData(
    player: MediampPlayer,
    videoUrl: String,
    audioUrl: String?,
    headers: Map<String, String>,
) {
    if (audioUrl == null) {
        // 无独立音频流，按普通单流处理
        player.setMediaData(UriMediaData(videoUrl, headers))
        return
    }
    // 先设置 pendingMediaSource（含视频+音频），resume 时由包装器应用
    val biliPlayer = player as BiliExoPlayerMediampPlayer
    biliPlayer.setPendingMediaSource(videoUrl, audioUrl, headers)
    // 通过 mediamp 设置视频流（状态→READY），resume 时用合并源覆盖
    player.setMediaData(UriMediaData(videoUrl, headers))
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

/**
 * 安卓端 actual：全屏播放时控制系统栏（状态栏/导航栏）的显示
 *
 * 通过 [WindowInsetsControllerCompat] 分别控制状态栏/导航栏（兼容 minSdk 21）。
 * 全屏时状态栏前景色固定为白色（视频画面为深色背景），
 * 隐藏时使用 `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`，允许用户从屏幕边缘滑动临时唤出系统栏。
 */
actual fun setPlayerFullscreenSystemBars(
    statusBarVisible: Boolean,
    navigationBarVisible: Boolean,
) {
    try {
        val activity = ActivityHolder.get() ?: return
        val window = activity.window
        val decorView = window.decorView
        val controller = WindowCompat.getInsetsController(window, decorView)
        if (statusBarVisible) {
            controller.show(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        }
        if (navigationBarVisible) {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        }
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // 全屏时状态栏前景色为白色（isAppearanceLightStatusBars = false）
        controller.isAppearanceLightStatusBars = false
    } catch (_: Exception) {}
}

/**
 * 安卓端 actual：恢复系统栏为默认状态
 *
 * 仅负责恢复系统栏显示；状态栏前景色由 MainActivity 在退出全屏时
 * 通过 StatusBarHelper 恢复。
 */
actual fun restorePlayerSystemBars() {
    try {
        val activity = ActivityHolder.get() ?: return
        val window = activity.window
        val decorView = window.decorView
        val controller = WindowCompat.getInsetsController(window, decorView)
        controller.show(WindowInsetsCompat.Type.systemBars())
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
