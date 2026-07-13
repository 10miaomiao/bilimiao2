package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowWidthSizeClass

/**
 * 判断当前窗口宽度是否属于 Compact 等级（< 600dp）。
 * 基于 Compose Multiplatform 官方 material3-adaptive 库的 [currentWindowAdaptiveInfo]，
 * 在 Android 与 Desktop 平台均可正确计算。
 */
@Composable
fun isCompactWindow(): Boolean {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
}
