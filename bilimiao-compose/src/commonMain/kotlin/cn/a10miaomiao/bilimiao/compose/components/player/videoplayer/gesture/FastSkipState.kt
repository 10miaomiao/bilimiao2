@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
 * 加速倍速由调用方按「当前倍速 × 倍数」计算, 本状态只负责开始/结束与票据配对.
 *
 * @param gestureIndicatorState 手势指示器状态, 用于显示快进图标
 * @param onStart 开始快进时调用, 参数为快进方向
 * @param onStop 结束快进时调用, 通常恢复原始倍速
 */
@Composable
fun rememberPlayerFastSkipState(
    gestureIndicatorState: GestureIndicatorState,
    onStart: (skipDirection: SkipDirection) -> Unit,
    onStop: () -> Unit,
): FastSkipState {
    // 长按手势通过 Modifier.pointerInput 长期持有回调, 若把 onStart/onStop 直接存进
    // remember, 手势触发时执行的是首次组合时的闭包, 其中捕获的播放倍速等状态会停留在
    // 组合初期 (表现为: 已切到 2.5x/3x/4x, 长按加速松手后却恢复成早期的 2x).
    // 用 rememberUpdatedState 让手势事件始终调用最新一次组合的回调.
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnStop by rememberUpdatedState(onStop)
    return remember {
        FastSkipState(
            onStart = { skipDirection -> currentOnStart(skipDirection) },
            onStop = { currentOnStop() },
        )
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
 * 长按手势的跨重组状态.
 *
 * 必须由 `remember` 持有: 长按加速会修改播放倍速并触发重组, 若这些状态随重组重建,
 * 松手时的票据校验会失败, 播放器就恢复不到长按前的倍速 (表现为倍速停留在长按加速值).
 */
@Stable
class LongPressGestureState {
    /** 当前手势内长按是否已触发. */
    var isLongPressDetected: Boolean = false

    /** 当前手势的票据, 由 [FastSkipState.startSkipping] 发放. */
    var ticket: Int = 0

    /** 最新一次组合的长按开始回调. */
    var onStart: (LongPressGestureState) -> Unit = {}

    /** 最新一次组合的长按结束回调. */
    var onEnd: (LongPressGestureState) -> Unit = {}
}

/**
 * 长按触发快进.
 *
 * 在 [direction] 方向上长按时调用 [FastSkipState.startSkipping],
 * 松手时调用 [FastSkipState.stopSkipping].
 */
@Composable
fun Modifier.longPressFastSkip(
    state: FastSkipState,
    direction: SkipDirection,
): Modifier = detectLongPressGesture(
    onStart = { gesture -> gesture.ticket = state.startSkipping(direction) },
    onEnd = { gesture -> state.stopSkipping(gesture.ticket) },
)

/**
 * 检测长按手势.
 *
 * 手指按下后若在 [longPressTimeout] 内未移动超过 touch slop, 则触发 [onStart];
 * 手指抬起时若长按已触发, 则调用 [onEnd]. 长按期间消费所有指针事件以阻止其他手势.
 *
 * 替代原 animeko 的同名实现, 使用 `pointerInput` + `awaitEachGesture`.
 */
@Composable
fun Modifier.detectLongPressGesture(
    onStart: (LongPressGestureState) -> Unit,
    onEnd: (LongPressGestureState) -> Unit,
    longPressTimeout: Long = 500L,
): Modifier {
    val gestureState = remember { LongPressGestureState() }
    // 组合期刷新回调: 手势协程长期持有创建时的闭包, 直接捕获参数会让手势读到
    // 组合初期的过期状态 (例如旧的播放倍速), 因此回调统一存放在状态对象里.
    gestureState.onStart = onStart
    gestureState.onEnd = onEnd
    // key 用状态对象而不是 lambda, 保证重组不会重置手势协程:
    // 一旦重置, 正在进行的长按会丢失结束回调, 倍速会停留在长按加速值.
    return pointerInput(gestureState) {
        try {
            coroutineScope {
                val touchSlop = viewConfiguration.touchSlop

                awaitEachGesture {
                    val initialPosition = awaitFirstDown(requireUnconsumed = false).position
                    // 注意: 不消费 down 事件

                    // 启动一个协程, 若用户在一定时间内未移动指针, 则标记为长按
                    val longPressJob = launch {
                        delay(longPressTimeout)
                        gestureState.onStart(gestureState)
                        gestureState.isLongPressDetected = true
                    }

                    var change = awaitPointerEvent()
                    while (change.changes.any { it.pressed }) { // 指针仍然按下
                        val pointer = change.changes[0]
                        if (gestureState.isLongPressDetected) {
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
                    if (gestureState.isLongPressDetected) {
                        // 消费指针 up 事件
                        change.changes.forEach { it.consume() }
                    }

                    longPressJob.cancel()
                    if (gestureState.isLongPressDetected) {
                        gestureState.isLongPressDetected = false
                        gestureState.onEnd(gestureState)
                    }
                }
            }
        } finally {
            // 组件被移除或手势协程被取消时, 长按可能仍在生效,
            // 必须补发结束回调, 否则播放器会一直停留在长按加速倍速.
            if (gestureState.isLongPressDetected) {
                gestureState.isLongPressDetected = false
                gestureState.onEnd(gestureState)
            }
        }
    }
}
