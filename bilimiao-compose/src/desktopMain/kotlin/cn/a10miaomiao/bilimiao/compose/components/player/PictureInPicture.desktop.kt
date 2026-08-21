package cn.a10miaomiao.bilimiao.compose.components.player

/**
 * 桌面端 actual：暂不支持画中画
 */
actual fun isPictureInPictureSupported(): Boolean = false

actual fun enterPictureInPictureMode(aspectWidth: Int, aspectHeight: Int): Boolean = false
