package cn.a10miaomiao.bilimiao.compose.platform

import android.app.Activity
import android.graphics.Color
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Android 端系统栏控制器（标准 WindowInsets 写法）。
 *
 * 构造时一次性完成窗口 edge-to-edge 配置：
 * - 状态栏区域由应用内容自绘（透明状态栏，配合 ComposeScaffold 的 insets 分发布局）；
 * - 内容布局延伸到系统栏区域（[WindowCompat.setDecorFitsSystemWindows]）。
 *
 * [setLightStatusBar] 仅切换状态栏前景图标明暗，[setSystemBarsVisible] / [restoreSystemBars]
 * 控制系统栏的显示与隐藏（全屏沉浸式）；三者均基于 [WindowInsetsControllerCompat]
 * 的独立维度，互不覆写。须在主线程调用（Compose LaunchedEffect / DisposableEffect
 * 均运行于主线程）。
 */
class SystemBarsControllerAndroid(
    private val activity: Activity,
) : SystemBarsController {

    init {
        // 1) 透明状态栏：由应用内容自绘状态栏背景
        activity.window.run {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            statusBarColor = Color.TRANSPARENT
        }
        // 2) edge-to-edge：内容布局延伸到状态栏/导航栏区域
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
    }

    override fun setLightStatusBar(light: Boolean) {
        insetsController().isAppearanceLightStatusBars = light
    }

    override fun setSystemBarsVisible(
        statusBarVisible: Boolean,
        navigationBarVisible: Boolean,
    ) {
        try {
            val controller = insetsController()
            if (statusBarVisible) {
                controller.show(WindowInsetsCompat.Type.statusBars())
            } else {
                controller.hide(WindowInsetsCompat.Type.statusBars())
            }
            if (navigationBarVisible) {
                controller.show(WindowInsetsCompat.Type.navigationBars())
            } else {
                controller.hide(WindowInsetsCompat.Type.navigationBars())
            }
            // 允许用户从屏幕边缘滑动临时唤出系统栏
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } catch (_: Exception) {}
    }

    override fun restoreSystemBars() {
        try {
            insetsController().show(WindowInsetsCompat.Type.systemBars())
        } catch (_: Exception) {}
    }

    private fun insetsController(): WindowInsetsControllerCompat {
        return WindowCompat.getInsetsController(activity.window, activity.window.decorView)
    }
}
