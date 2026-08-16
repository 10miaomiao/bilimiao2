@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 键盘调整倍速时的步长, 与 Slider 步长保持一致. */
private const val KEYBOARD_SPEED_STEP: Float = 0.1f

/**
 * 将键盘输入转换为提交给调用方的最终倍速.
 *
 * @param currentSpeed 当前倍速
 * @param range 允许的倍速范围
 * @param direction 调整方向, `-1` 减速, `1` 加速
 */
fun nextPlaybackSpeed(
    currentSpeed: Float,
    range: ClosedFloatingPointRange<Float>,
    direction: Int,
): Float = (currentSpeed + direction * KEYBOARD_SPEED_STEP).coerceIn(range)

/**
 * 倍速控制 (SpeedSwitcher, 键盘快捷键) 的 state object.
 *
 * 简化自 animeko 的 [PlaybackSpeedControllerState]: 移除了对 mediamp `PlaybackSpeed` feature
 * 与 coroutine scope 的依赖, 通过 [getCurrentSpeed] / [onSetSpeed] 回调对接具体播放器,
 * 保持 [currentSpeed]、[speedRange]、[commitSpeed]、[DEFAULT_SPEED_RANGE] 的语义一致.
 *
 * @param getCurrentSpeed 获取当前播放器倍速, 每次读取 [currentSpeed] 时重新求值
 * @param rangeProvider 用户配置的倍速范围, 每次读取 [speedRange] 时都会重新求值
 * @param onSetSpeed 倍速变更回调, 通常转发给播放器并持久化
 */
@Stable
class PlaybackSpeedControllerState(
    getCurrentSpeed: () -> Float,
    rangeProvider: () -> ClosedFloatingPointRange<Float> = { DEFAULT_SPEED_RANGE },
    private val onSetSpeed: (Float) -> Unit = {},
) {
    val speedRange: ClosedFloatingPointRange<Float> by derivedStateOf(rangeProvider)

    var currentSpeed: Float by mutableStateOf(getCurrentSpeed())
        private set

    /**
     * 拖动 Slider 期间实时预览倍速, 不触发 [onSetSpeed].
     */
    fun previewSpeed(value: Float) {
        applySpeed(value)
    }

    /**
     * 提交最终倍速: 应用到播放器并回调 [onSetSpeed].
     */
    fun commitSpeed(value: Float) {
        applySpeed(value)
        onSetSpeed(value)
    }

    private fun applySpeed(value: Float) {
        currentSpeed = value
        onSetSpeed(value)
    }

    companion object {
        val DEFAULT_SPEED_RANGE: ClosedFloatingPointRange<Float> = 0.5f..2.5f
    }
}
