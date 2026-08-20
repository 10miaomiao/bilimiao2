package com.a10miaomiao.bilimiao.comm.delegate.player

import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.mpv.MPVHandle
import org.openani.mediamp.mpv.MpvMediampPlayer
import org.openani.mediamp.source.UriMediaData

/**
 * 桌面端 actual：创建 MpvMediampPlayer 并加载 mpv native 库
 */
actual fun createMediampPlayer(): MediampPlayer {
    initMpvNativeLibraries()
    return MpvMediampPlayer(Unit, kotlinx.coroutines.Dispatchers.Main)
}

private fun initMpvNativeLibraries() {
    // Native libraries are extracted automatically by mediamp-native-loader
    // from the classpath (mediamp-mpv-runtime-windows-x64 JAR)
    MPVHandle.useDefaultRuntimeLibraryDirectory()
}

/**
 * 桌面端 actual：通过 mpv 的 audio-files 属性注入外部音频流
 */
actual suspend fun setMergingMediaData(
    player: MediampPlayer,
    videoUrl: String,
    audioUrl: String?,
    headers: Map<String, String>,
) {
    player.setMediaData(UriMediaData(videoUrl, headers))
    if (player is MpvMediampPlayer && audioUrl != null) {
        (player.impl as MPVHandle).setPropertyString("audio-files", audioUrl)
        println("[BiliMiao] Set external audio: $audioUrl")
    }
}

/**
 * 桌面端 actual：通过 mpv 的 volume 属性设置音量
 */
actual fun setPlayerVolume(player: MediampPlayer, volume: Int) {
    try {
        if (player is MpvMediampPlayer) {
            (player.impl as MPVHandle).setPropertyInt("volume", volume)
        }
    } catch (_: Exception) {}
}

/**
 * 桌面端 actual：no-op（桌面端无屏幕方向概念，全屏通过窗口管理处理）
 */
actual fun setRequestedOrientation(request: ScreenOrientationRequest) {
    // no-op: 桌面端通过 WindowsWindowUtils.setUndecoratedFullscreen 处理全屏
}

/**
 * 桌面端 actual：no-op（桌面端无系统状态栏/导航条概念）
 */
actual fun setPlayerFullscreenSystemBars(
    statusBarVisible: Boolean,
    navigationBarVisible: Boolean,
) {
    // no-op
}

/**
 * 桌面端 actual：no-op（桌面端无系统状态栏/导航条概念）
 */
actual fun restorePlayerSystemBars() {
    // no-op
}
