package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress

import cn.a10miaomiao.bilimiao.compose.common.platform.Platform
import cn.a10miaomiao.bilimiao.compose.common.platform.platformInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.DesktopFullscreenMode
import com.a10miaomiao.bilimiao.comm.delegate.player.FullscreenMode

/**
 * 全屏方式菜单项
 *
 * [value] 的含义随平台变化：安卓是 [FullscreenMode] 的屏幕方向值，
 * 桌面是 [DesktopFullscreenMode] 的全屏方式值。
 */
data class FullscreenMenuOption(val value: Int, val label: String)

/**
 * 安卓端「全屏播放屏幕方向」的选项顺序
 *
 * 与设置页保持一致（对照旧版 `R.menu.player_full_mode`），菜单和设置页共用这一份顺序与文案。
 */
val fullscreenModeMenuOrder: List<FullscreenMode> = listOf(
    FullscreenMode.AUTO,
    FullscreenMode.UNSPECIFIED,
    FullscreenMode.SENSOR_LANDSCAPE,
    FullscreenMode.LANDSCAPE,
    FullscreenMode.REVERSE_LANDSCAPE,
)

/**
 * 安卓端「全屏播放屏幕方向」的显示文案
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

/**
 * 桌面端「全屏播放方式」的选项顺序
 *
 * 桌面窗口没有屏幕方向，只需区分窗口是否进入全屏，故只有两个选项。
 */
val desktopFullscreenModeOrder: List<DesktopFullscreenMode> = listOf(
    DesktopFullscreenMode.SCREEN,
    DesktopFullscreenMode.WINDOW,
)

/** 桌面端「全屏播放方式」的显示文案 */
fun desktopFullscreenModeLabel(mode: DesktopFullscreenMode): String = when (mode) {
    DesktopFullscreenMode.SCREEN -> "铺满屏幕"
    DesktopFullscreenMode.WINDOW -> "铺满窗口"
}

/**
 * 当前平台的全屏方式菜单项
 *
 * 安卓端为屏幕方向，桌面端为「铺满屏幕 / 铺满窗口」。
 */
fun fullscreenMenuOptions(): List<FullscreenMenuOption> {
    return if (platformInfo.platform == Platform.DESKTOP) {
        desktopFullscreenModeOrder.map { FullscreenMenuOption(it.value, desktopFullscreenModeLabel(it)) }
    } else {
        fullscreenModeMenuOrder.map { FullscreenMenuOption(it.value, fullscreenModeLabel(it)) }
    }
}
