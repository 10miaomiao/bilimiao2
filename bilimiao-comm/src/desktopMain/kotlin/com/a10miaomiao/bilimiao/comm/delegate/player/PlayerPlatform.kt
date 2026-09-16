package com.a10miaomiao.bilimiao.comm.delegate.player

import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.mpv.MPVHandle
import org.openani.mediamp.mpv.MpvMediampPlayer

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
 * 桌面端 actual：通过 mpv 的 audio-files 属性接入外部音频流
 *
 * mpv 的 `audio-files` 在 loadfile 时生效，而 mediamp 的 `setMediaData` 内部就是执行
 * loadfile，因此本函数必须在 `setMediaData` 之前调用（见 [setExternalAudioTrack]）。
 *
 * 注意这里修的是「新视频响上一个视频的声音」：
 * - 之前是在 `setMediaData` 之后设置该属性 → 音频晚一个视频生效，当前视频无声；
 * - 之前 `audioUrl` 为 null 时不做处理 → mpv 的 audio-files 跨文件保留，旧音频一直沿用。
 */
actual fun setExternalAudioTrack(
    player: MediampPlayer,
    videoUrl: String,
    audioUrl: String?,
    headers: Map<String, String>,
) {
    val handle = (player as? MpvMediampPlayer)?.impl as? MPVHandle ?: return
    // audioUrl 为 null → 显式清空，避免沿用上一个视频的音频
    val ok = handle.setPropertyString("audio-files", audioUrl.orEmpty())
    println("[BiliMiao] external audio: ${audioUrl ?: "(none)"} (mpv accepted: $ok)")
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
