package cn.a10miaomiao.bilimiao.compose.platform

/**
 * 桌面端系统栏控制器。
 *
 * 桌面窗口没有系统状态栏，窗口顶部留给“状态栏”的位置实际是自绘的标题栏
 * （见 desktop-app 的 WindowsWindowFrame），因此把 [setLightStatusBar] 映射为标题栏
 * 前景（标题栏按钮图标）的明暗请求，由宿主窗口通过 [onLightStatusBarChanged] 决定具体配色。
 *
 * [setSystemBarsVisible] / [restoreSystemBars] 保持空实现：桌面窗口没有可显隐的系统栏，
 * 全屏（沉浸式）时的窗口形态由宿主窗口自行处理。
 *
 * @param onLightStatusBarChanged 状态栏前景色变化的回调，参数含义同 [setLightStatusBar]：
 * true 表示状态栏区域为浅色背景（标题栏应使用深色前景），false 表示深色背景（浅色前景）。
 */
class SystemBarsControllerDesktop(
    private val onLightStatusBarChanged: (light: Boolean) -> Unit = {},
) : SystemBarsController {

    override fun setLightStatusBar(light: Boolean) {
        onLightStatusBarChanged(light)
    }

    override fun setSystemBarsVisible(
        statusBarVisible: Boolean,
        navigationBarVisible: Boolean
    ) {
    }

    override fun restoreSystemBars() {
    }
}