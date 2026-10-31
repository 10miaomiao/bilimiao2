package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 当前窗口像素宽度（px），由平台入口写入。
 *
 * Android 端（API 34 之前）由 MainActivity 通过内容区的 OnLayoutChangeListener
 * 实时写入真实窗口宽度，用于规避系统「尺寸类配置变化不派发到 View / 不刷新
 * `LocalWindowInfo`」的 bug——该 bug 在 API 30~33 上均存在，会导致官方
 * `currentWindowAdaptiveInfo()` 依赖的 `LocalWindowInfo.containerSize` 停留在旧值，
 * 典型表现是退出全屏由横屏转回竖屏后骨架布局仍按横屏排布（appbar 卡在侧边栏）。
 * 为 null 表示平台层未写入（API 34 起系统已修复、桌面端无窗口尺寸概念），
 * 此时回退到官方库计算。
 */
object WindowSizeState {
    val widthPx = MutableStateFlow<Int?>(null)
}

/**
 * 判断当前窗口宽度是否属于 Compact 等级（< 600dp）。
 *
 * 优先读取 [WindowSizeState]（平台入口实时写入的窗口宽度，事件驱动、不依赖系统
 * 配置变化派发）；未初始化时回退到 material3-adaptive 官方库的
 * [currentWindowAdaptiveInfo]，桌面端窗口尺寸变化仍由官方库正常驱动。
 */
@Composable
fun isCompactWindow(): Boolean {
    val density = LocalDensity.current
    val widthPx by WindowSizeState.widthPx.collectAsState()
    return if (widthPx != null) {
        with(density) { widthPx!!.toDp() } < 600.dp
    } else {
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        !windowSizeClass.isWidthAtLeastBreakpoint(600)
    }
}
