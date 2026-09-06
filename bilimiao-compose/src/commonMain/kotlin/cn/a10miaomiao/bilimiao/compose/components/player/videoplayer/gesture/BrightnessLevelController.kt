package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.runtime.Composable

/**
 * 创建用于屏幕左侧滑动手势调节亮度的 [LevelController].
 *
 * 亮度调节依赖平台窗口能力, 因此按平台提供实现:
 * - 安卓: 控制 Activity 窗口亮度 ([android.view.WindowManager.LayoutParams.screenBrightness]),
 *   离开组合 (播放器关闭等) 时恢复为手势前的亮度;
 * - 桌面: 窗口系统不提供逐窗口亮度, 返回 [NoOpLevelController].
 */
@Composable
expect fun rememberBrightnessLevelController(): LevelController
