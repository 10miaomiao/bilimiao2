package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import cn.a10miaomiao.bilimiao.compose.platform.LocalPlatformContext
import cn.a10miaomiao.bilimiao.compose.platform.PlatformContext

/**
 * 安卓端 actual: 通过 Activity 窗口亮度实现亮度手势控制.
 *
 * 窗口亮度 (`WindowManager.LayoutParams.screenBrightness`) 取值:
 * - `-1` (默认) 表示跟随系统自动亮度, 此时显示基准取系统亮度设置;
 * - `0f..1f` 表示本窗口独立亮度.
 *
 * 窗口读写统一经 compose 层 [PlatformContext] (由 MainActivity 以自身窗口创建,
 * 随 Activity 重建换新实例);
 * 组件离开组合 (如播放器关闭) 时恢复为手势前的亮度, 避免修改残留在窗口上.
 */
@Composable
actual fun rememberBrightnessLevelController(): LevelController {
    val platformContext = LocalPlatformContext.current
    val controller = remember(platformContext) { AndroidBrightnessLevelController(platformContext) }
    DisposableEffect(controller) {
        onDispose(controller::restore)
    }
    return controller
}

private class AndroidBrightnessLevelController(
    private val platformContext: PlatformContext,
) : LevelController {
    override val range: ClosedRange<Float> = 0f..1f
    override val levelStep: Float get() = 0.01f

    /** 手势前的窗口亮度 (-1 表示跟随系统自动亮度), 用于退出时恢复 */
    private val originalBrightness: Float = platformContext.getWindowBrightness()

    /** 最近一次设置/读取到的亮度, 自动亮度 (窗口亮度为 -1) 时作为显示与调整的基准 */
    private var lastLevel: Float =
        originalBrightness.takeIf { it in 0f..1f } ?: platformContext.getSystemBrightness()

    override val level: Float
        get() = platformContext.getWindowBrightness().takeIf { it in 0f..1f } ?: lastLevel

    override fun setLevel(level: Float) {
        val clamped = level.coerceIn(range.start, range.endInclusive)
        lastLevel = clamped
        platformContext.setWindowBrightness(clamped)
    }

    fun restore() {
        platformContext.setWindowBrightness(originalBrightness)
    }
}
