@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 记住一个基于回调的快进状态.
 *
 * 简化自 animeko 的 `rememberPlayerFastSkipState`: 移除了 mediamp `PlaybackSpeed` 依赖,
 * 调用方通过 [onStart]/[onStop] 直接操作播放器倍速.
 *
 * @param gestureIndicatorState 手势指示器状态, 用于显示快进图标
 * @param onStart 开始快进时调用, 参数为快进方向
 * @param onStop 结束快进时调用, 通常恢复原始倍速
 * @param fastForwardSpeed 快进倍速, 默认 3 倍
 */
@Composable
fun rememberPlayerFastSkipState(
    gestureIndicatorState: GestureIndicatorState,
    onStart: (skipDirection: SkipDirection) -> Unit,
    onStop: () -> Unit,
    fastForwardSpeed: Float = 3f,
): FastSkipState {
    return remember(fastForwardSpeed) {
        FastSkipState(onStart = onStart, onStop = onStop)
    }
}

/**
 * 快进状态, 跟踪当前是否正在快进以及对应的票据.
 *
 * [startSkipping] 与 [stopSkipping] 通过票据匹配, 避免多次触发导致状态错乱.
 */
@Stable
class FastSkipState(
    private val onStart: (skipDirection: SkipDirection) -> Unit,
    private val onStop: () -> Unit,
) {
    private var skippingDirection: SkipDirection? by mutableStateOf(null)
    private var ticket: Int = 0

    fun startSkipping(direction: SkipDirection): Int {
        skippingDirection = direction
        onStart(direction)
        return ++ticket
    }

    fun stopSkipping(ticket: Int) {
        if (ticket == this.ticket) {
            skippingDirection = null
            onStop()
        }
    }
}

/** 快进方向. 目前仅支持 [FORWARD]. */
enum class SkipDirection {
    FORWARD, BACKWARD
}

/**
 * 长按触发快进.
 *
 * 在 [direction] 方向上长按时调用 [FastSkipState.startSkipping],
 * 松手时调用 [FastSkipState.stopSkipping].
 */
fun Modifier.longPressFastSkip(
    state: FastSkipState,
    direction: SkipDirection,
): Modifier {
    var ticket = 0
    return detectLongPressGesture(
        onStart = {
            ticket = state.startSkipping(direction)
        },
        onEnd = {
            state.stopSkipping(ticket)
        },
    )
}

/**
 * 检测长按手势.
 *
 * 手指按下后若在 [longPressTimeout] 内未移动超过 touch slop, 则触发 [onStart];
 * 手指抬起时若长按已触发, 则调用 [onEnd]. 长按期间消费所有指针事件以阻止其他手势.
 *
 * 替代原 animeko 的同名实现, 使用 `pointerInput` + `awaitEachGesture`.
 */
fun Modifier.detectLongPressGesture(
    onStart: () -> Unit,
    onEnd: () -> Unit,
    longPressTimeout: Long = 500L,
): Modifier = pointerInput(Unit) {
    coroutineScope {
        val touchSlop = viewConfiguration.touchSlop
        var isLongPressDetected = false

        awaitEachGesture {
            val initialPosition = awaitFirstDown(requireUnconsumed = false).position
            // 注意: 不消费 down 事件

            // 启动一个协程, 若用户在一定时间内未移动指针, 则标记为长按
            val longPressJob = launch {
                delay(longPressTimeout)
                onStart()
                isLongPressDetected = true
            }

            var change = awaitPointerEvent()
            while (change.changes.any { it.pressed }) { // 指针仍然按下
                val pointer = change.changes[0]
                if (isLongPressDetected) {
                    // 消费所有事件, 阻止滑动等其他手势
                    change.changes.forEach { it.consume() }
                }
                if ((pointer.position - initialPosition).getDistance() > touchSlop) {
                    // 用户正在滑动. 即使长按已触发也取消检测
                    longPressJob.cancel()
                }
                change = awaitPointerEvent()
            }
            // 指针已抬起
            if (isLongPressDetected) {
                // 消费指针 up 事件
                change.changes.forEach { it.consume() }
            }

            longPressJob.cancel()
            if (isLongPressDetected) {
                onEnd()
                isLongPressDetected = false
            }
        }
    }
}
