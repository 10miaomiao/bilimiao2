package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.runtime.Composable

/**
 * 桌面端 actual: 桌面端不接管系统音量, 返回 `null`, 由调用方回退到播放器自身音量.
 */
@Composable
actual fun rememberSystemVolumeLevelController(): LevelController? = null
