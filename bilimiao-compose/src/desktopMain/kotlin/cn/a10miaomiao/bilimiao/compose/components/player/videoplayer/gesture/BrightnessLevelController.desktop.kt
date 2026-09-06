package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.runtime.Composable

/**
 * 桌面端 actual: 窗口系统不提供逐窗口亮度调节, 手势亮度调节为 no-op
 * (PlayerGestureHost 按 [NoOpLevelController] 判断并跳过挂载亮度手势).
 */
@Composable
actual fun rememberBrightnessLevelController(): LevelController = NoOpLevelController
