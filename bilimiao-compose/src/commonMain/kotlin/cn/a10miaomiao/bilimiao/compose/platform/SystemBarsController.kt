package cn.a10miaomiao.bilimiao.compose.platform

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 系统栏（状态栏/导航栏）统一控制器（平台工具类）。
 *
 * 将系统栏的“显示/隐藏”与“状态栏前景色”职责集中到一个平台实现上，
 * 避免多套平台代码各自操作同一个窗口互相覆盖：
 * - UI 层：ComposeScaffold 依据播放器显示模式/深色主题调用 [setLightStatusBar]；
 *   全屏播放器（BiliVideoScaffold）只调用 [setSystemBarsVisible] / [restoreSystemBars]。
 * - 平台层：Android 端在入口组合中注入实现（[LocalSystemBarsController]）；
 *   桌面端在自绘标题栏（Windows 窗口框架）中注入 SystemBarsControllerDesktop，
 *   把前景色映射为标题栏按钮的配色。
 *
 * 所有操作均须在主线程调用。
 */
interface SystemBarsController {
    /**
     * 设置状态栏前景图标明暗。
     * @param light true → 深色图标（适合浅色背景）；false → 白色图标（适合深色背景）
     */
    fun setLightStatusBar(light: Boolean)

    /**
     * 设置状态栏/导航栏的显示与隐藏（全屏沉浸式）。
     *
     * @param statusBarVisible 是否显示状态栏
     * @param navigationBarVisible 是否显示导航栏
     */
    fun setSystemBarsVisible(
        statusBarVisible: Boolean,
        navigationBarVisible: Boolean,
    )

    /** 恢复状态栏/导航栏为默认（全部显示）状态 */
    fun restoreSystemBars()
}

/** 无操作实现：桌面等无系统栏的平台使用 */
object NoopSystemBarsController : SystemBarsController {
    override fun setLightStatusBar(light: Boolean) {
    }

    override fun setSystemBarsVisible(
        statusBarVisible: Boolean,
        navigationBarVisible: Boolean,
    ) {
    }

    override fun restoreSystemBars() {
    }
}

/** 当前组合作用域的系统栏控制器；Android 端由入口 host 提供真实实现 */
val LocalSystemBarsController = staticCompositionLocalOf<SystemBarsController> {
    NoopSystemBarsController
}
