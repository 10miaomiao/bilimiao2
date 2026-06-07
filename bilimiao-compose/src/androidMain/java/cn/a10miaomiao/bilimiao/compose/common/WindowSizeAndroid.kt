package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowWidthSizeClass

@Composable
actual fun isCompactWindow(): Boolean {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
}
