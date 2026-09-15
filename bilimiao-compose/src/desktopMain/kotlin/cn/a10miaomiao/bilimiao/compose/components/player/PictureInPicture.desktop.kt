package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.runtime.Composable

/**
 * 桌面端 actual：暂不支持画中画
 */
actual fun isPictureInPictureSupported(): Boolean = false

actual fun enterPictureInPictureMode(aspectWidth: Int, aspectHeight: Int, isPlaying: Boolean): Boolean = false

/**
 * 桌面端 actual：暂不支持画中画，动作按钮为空实现
 */
@Composable
actual fun PictureInPicturePlaybackAction(
    enabled: Boolean,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
) {
}
