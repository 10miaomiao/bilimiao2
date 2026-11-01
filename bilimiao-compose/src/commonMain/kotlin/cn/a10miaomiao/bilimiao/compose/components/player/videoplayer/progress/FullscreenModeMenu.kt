package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress

import com.a10miaomiao.bilimiao.comm.delegate.player.FullscreenMode

/**
 * 全屏播放屏幕方向的选项顺序
 *
 * 与设置页「播放设置 - 全屏播放屏幕方向」保持一致（对照旧版 `R.menu.player_full_mode`），
 * 播放器长按全屏按钮弹出的菜单和设置页共用这一份顺序与文案。
 */
val fullscreenModeMenuOrder: List<FullscreenMode> = listOf(
    FullscreenMode.AUTO,
    FullscreenMode.UNSPECIFIED,
    FullscreenMode.SENSOR_LANDSCAPE,
    FullscreenMode.LANDSCAPE,
    FullscreenMode.REVERSE_LANDSCAPE,
)

/**
 * 全屏播放屏幕方向的显示文案
 *
 * 对照旧版 `player_full_mode` 菜单项与设置页的文案，两处必须一致。
 */
fun fullscreenModeLabel(mode: FullscreenMode): String = when (mode) {
    FullscreenMode.AUTO -> "跟随视频"
    FullscreenMode.UNSPECIFIED -> "跟随系统"
    FullscreenMode.SENSOR_LANDSCAPE -> "横向全屏(自动)"
    FullscreenMode.LANDSCAPE -> "横向全屏(固定方向1)"
    FullscreenMode.REVERSE_LANDSCAPE -> "横向全屏(固定方向2)"
}
