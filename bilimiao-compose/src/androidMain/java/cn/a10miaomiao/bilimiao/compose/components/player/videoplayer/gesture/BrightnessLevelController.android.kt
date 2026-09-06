package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import android.app.Activity
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.a10miaomiao.bilimiao.comm.delegate.player.ActivityHolder

/**
 * 安卓端 actual: 通过 Activity 窗口亮度实现亮度手势控制.
 *
 * 窗口亮度 ([android.view.WindowManager.LayoutParams.screenBrightness]) 取值:
 * - `-1` (默认) 表示跟随系统自动亮度, 此时显示基准取系统亮度设置 [Settings.System.SCREEN_BRIGHTNESS];
 * - `0f..1f` 表示本窗口独立亮度.
 *
 * 组件离开组合 (如播放器关闭) 时恢复为手势前的亮度, 避免修改残留在全局窗口上.
 */
@Composable
actual fun rememberBrightnessLevelController(): LevelController {
    val controller = remember { AndroidBrightnessLevelController() }
    DisposableEffect(controller) {
        onDispose(controller::restore)
    }
    return controller
}

private class AndroidBrightnessLevelController : LevelController {
    override val range: ClosedRange<Float> = 0f..1f
    override val levelStep: Float get() = 0.01f

    /** 手势前的窗口亮度 (-1 表示跟随系统自动亮度), 用于退出时恢复 */
    private val originalBrightness: Float = try {
        activity?.window?.attributes?.screenBrightness ?: -1f
    } catch (_: Exception) {
        -1f
    }

    /** 最近一次设置/读取到的亮度, 自动亮度 (窗口亮度为 -1) 时作为显示与调整的基准 */
    private var lastLevel: Float = originalBrightness.takeIf { it in 0f..1f } ?: systemBrightness()

    override val level: Float
        get() {
            val windowBrightness = try {
                activity?.window?.attributes?.screenBrightness
            } catch (_: Exception) {
                null
            }
            return if (windowBrightness != null && windowBrightness in 0f..1f) {
                windowBrightness
            } else {
                lastLevel
            }
        }

    override fun setLevel(level: Float) {
        val clamped = level.coerceIn(range.start, range.endInclusive)
        lastLevel = clamped
        val window = activity?.window ?: return
        try {
            window.attributes = window.attributes.apply {
                screenBrightness = clamped
            }
        } catch (_: Exception) {
        }
    }

    fun restore() {
        val window = activity?.window ?: return
        try {
            window.attributes = window.attributes.apply {
                screenBrightness = originalBrightness
            }
        } catch (_: Exception) {
        }
    }

    private val activity: Activity?
        get() = try {
            ActivityHolder.get()
        } catch (_: Exception) {
            null
        }

    /** 读取系统亮度设置 (0-255) 作为自动亮度模式下的显示基准, 读取失败时退回 50% */
    private fun systemBrightness(): Float = try {
        val resolver = activity?.contentResolver ?: return 0.5f
        Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS).toFloat() / 255f
    } catch (_: Exception) {
        0.5f
    }
}
