package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.runtime.Composable

/**
 * 创建用于屏幕右侧滑动手势调节音量的 [LevelController].
 *
 * 调节目标按平台不同:
 * - 安卓: 直接控制系统媒体音量 ([android.media.AudioManager] 的 `STREAM_MUSIC`),
 *   与硬件音量键作用于同一个音量流, 退出播放器后音量保持 (不恢复);
 * - 桌面: 桌面端不接管系统音量, 返回 `null`, 由调用方回退到播放器自身音量.
 */
@Composable
expect fun rememberSystemVolumeLevelController(): LevelController?
