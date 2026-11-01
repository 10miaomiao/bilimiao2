package cn.a10miaomiao.bilimiao.compose.common.platform

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 桌面窗口自绘标题栏占用的高度，其他平台恒为 0
 *
 * 桌面窗口没有系统边框，标题栏由宿主自绘并叠在窗口内容之上（见 desktop-app 的
 * WindowsWindowFrame）。铺满窗口的界面（如播放器全屏）必须避让这段高度，
 * 否则内容会被标题栏覆盖，且该区域的鼠标按下会被窗口判定为内容点击而不是拖动窗口。
 *
 * 由桌面宿主在窗口框架内提供，安卓端保持默认的 0。
 */
val LocalWindowTitleBarInsets = staticCompositionLocalOf { WindowInsets(0, 0, 0, 0) }
