package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import android.content.Context
import android.media.AudioManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlin.math.roundToInt

/**
 * 安卓端 actual: 通过 [AudioManager] 直接控制系统媒体音量 (`STREAM_MUSIC`).
 *
 * 与硬件音量键作用于同一个音量流, 因此退出播放器后音量设置保留 (不做恢复),
 * 行为与系统音量键一致.
 *
 * 系统音量是离散档位 (通常 15 档), 而手势按连续值累加 (步进 0.05):
 * 若每次都从系统读回档位再叠加, 小于一档的步进会被 round 掉导致手势"不跟手".
 * 因此内部保留连续值 [continuousLevel], 仅在检测到外部改动 (硬件音量键、
 * 其他应用调整) 时以系统档位重新同步.
 */
@Composable
actual fun rememberSystemVolumeLevelController(): LevelController? {
    val context = LocalContext.current
    return remember(context) { AndroidSystemVolumeLevelController(context) }
}

private class AndroidSystemVolumeLevelController(
    context: Context,
) : LevelController {
    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    override val range: ClosedRange<Float> = 0f..1f

    /** 系统最大音量档位; 获取失败时按 15 档 (多数设备的媒体音量档位数) 估算 */
    private val maxSteps: Int
        get() = audioManager
            ?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            ?.takeIf { it > 0 }
            ?: DEFAULT_MAX_STEPS

    /** 该控制器能表示的最小变化: 一个系统音量档位 */
    override val levelStep: Float
        get() = 1f / maxSteps

    /** 手势累积的连续音量 (0f..1f) */
    private var continuousLevel: Float = readLevel()

    override val level: Float
        get() {
            val systemLevel = readLevel()
            // 系统音量被外部改动后连续值失效, 以系统值重新同步
            if (toStep(systemLevel) != toStep(continuousLevel)) {
                continuousLevel = systemLevel
            }
            return continuousLevel
        }

    override fun setLevel(level: Float) {
        continuousLevel = level.coerceIn(range.start, range.endInclusive)
        writeLevel(continuousLevel)
    }

    private fun toStep(level: Float): Int = (level * maxSteps).roundToInt()

    private fun readLevel(): Float = try {
        val current = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        current.toFloat() / maxSteps
    } catch (_: Exception) {
        0f
    }

    private fun writeLevel(level: Float) {
        val manager = audioManager ?: return
        try {
            // flags 传 0: 不弹系统音量条, 避免与播放器手势指示器重复显示
            val target = (level * maxSteps).roundToInt().coerceIn(0, maxSteps)
            manager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        } catch (_: Exception) {
            // 勿扰模式等场景可能抛 SecurityException, 静默忽略
        }
    }

    private companion object {
        const val DEFAULT_MAX_STEPS = 15
    }
}
