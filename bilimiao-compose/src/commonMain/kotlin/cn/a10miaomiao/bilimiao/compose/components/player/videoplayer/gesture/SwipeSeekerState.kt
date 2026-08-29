@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.annotation.UiThread
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlin.math.roundToInt
import kotlin.math.sign


@Composable
fun rememberSwipeSeekerState(
    screenWidthPx: Int,
    swipeSeekerConfig: SwipeSeekerConfig = SwipeSeekerConfig.Default,
    @UiThread onSeek: (offsetSeconds: Int) -> Unit,
): SwipeSeekerState {
    val onSeekState by rememberUpdatedState(onSeek)
    val density = LocalDensity.current
    // draggable 识别手势时会先扣除一次 touch slop, 这里把该值传入状态用于补偿位移
    val touchSlopPx = LocalViewConfiguration.current.touchSlop
    return remember(swipeSeekerConfig, screenWidthPx, density, touchSlopPx) {
        SwipeSeekerState(
            screenWidthPx,
            swipeSeekerConfig,
            density,
            touchSlopPx,
        ) { onSeekState(it) }
    }
}

@Immutable
data class SwipeSeekerConfig(
    /**
     * 从屏幕左边滑到屏幕的最右边的最大距离
     */
    val maxDragDelta: Float = 0f,
    /**
     * 从屏幕左边滑到屏幕的最右边会跳转的秒数
     */
    // 设计上是从左到右 90 秒正好跳过 op/ed, 而全面屏手机有全面屏手势,
    // 用户不能从最左边开始滑. 因此稍微留了点余量.
    // 实测差不多可以滑到 87 秒, 看三秒 op 让他知道他完了 op
    val maxDragSeconds: Int = 97,
    /**
     * 向上滑动多少距离后取消本次快进.
     *
     * 快进过程中手指向上移动超过该距离即取消, 滑回该距离以内恢复.
     */
    val cancelVerticalDragDistance: Dp = 144.dp,
) {
    companion object {
        val Default = SwipeSeekerConfig()
    }
}

internal fun isVerticalDragCancelled(
    dragStartY: Float,
    position: Offset,
    cancelVerticalDragDistancePx: Float,
): Boolean {
    return position.isSpecified &&
        dragStartY - position.y > cancelVerticalDragDistancePx
}

/**
 * 跟踪滑动取消状态.
 *
 * 使用 `pointerInput` + `awaitPointerEventScope` 在 [PointerEventPass.Initial] 阶段监听指针事件,
 * 替代原 animeko 的 `onPointerEventMultiplatform`.
 */
private fun Modifier.trackSwipeSeekCancellation(
    seekerState: SwipeSeekerState,
    onCancellationChanged: (Boolean) -> Unit,
): Modifier = this.pointerInput(seekerState) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            when (event.type) {
                PointerEventType.Press -> {
                    event.changes.firstOrNull()?.let { seekerState.onPointerDown(it.position) }
                }

                PointerEventType.Move -> {
                    val change = event.changes.firstOrNull() ?: continue
                    if (seekerState.updateCancellation(change.position)) {
                        onCancellationChanged(seekerState.isCancelled)
                    }
                }

                else -> {}
            }
        }
    }
}

@Stable
class SwipeSeekerState internal constructor(
    /**
     * 可滑动区域宽度
     */
    private val screenWidthPx: Int,
    private val swipeSeekerConfig: SwipeSeekerConfig,
    density: Density,
    /**
     * touch slop (px). [androidx.compose.foundation.gestures.draggable] 识别拖动手势时
     * 会先扣除一次 touch slop 再上报位移, 计算秒数时需要补回, 否则快进/快退会始终
     * 落后于手指实际位置.
     */
    private val touchSlopPx: Float,
    /**
     * 当一次滑动结束时的回调. `offsetSeconds` 为本次快进的秒数
     */
    @UiThread val onSeek: (offsetSeconds: Int) -> Unit,
) {
    private val cancelVerticalDragDistancePx =
        with(density) { swipeSeekerConfig.cancelVerticalDragDistance.toPx() }

    /**
     * [Float.NaN] 表示未在滑动
     */
    private var seekDelta: Float by mutableFloatStateOf(Float.NaN)

    /**
     * 当前滑动是否已取消, 即手指是否已向上移动超过取消距离.
     */
    var isCancelled: Boolean by mutableStateOf(false)
        private set

    /**
     * 手指按下位置的 Y 坐标, 作为取消判定的基准线. [Float.NaN] 表示未在滑动.
     *
     * 基准取按下点而不是拖动手势识别点: 滑动大概率不是直的, 手势识别 (越过 touch slop)
     * 时手指可能已经有垂直偏移, 以识别点为基准会把这部分偏移吃掉.
     */
    private var dragStartY: Float = Float.NaN

    @UiThread
    internal fun onPointerDown(position: Offset) {
        if (!isSeeking && position.isSpecified) {
            dragStartY = position.y
        }
    }

    @UiThread
    internal fun onSwipeStarted() {
        seekDelta = 0f
        isCancelled = false
    }

    @UiThread
    internal fun onSwipeStopped() {
        if (seekDelta.isNaN()) return
        if (!isCancelled) {
            onSeek(deltaSeconds)
        }
        seekDelta = Float.NaN
        isCancelled = false
        dragStartY = Float.NaN
    }

    @UiThread
    internal fun onSwipeOffset(offsetPx: Float) {
        seekDelta += offsetPx
    }

    @UiThread
    internal fun updateCancellation(position: Offset): Boolean {
        val wasCancelled = isCancelled
        if (isSeeking) {
            isCancelled = isVerticalDragCancelled(dragStartY, position, cancelVerticalDragDistancePx)
        }
        return isCancelled != wasCancelled
    }

    /**
     * 是否正在快进, 即用户是否正在滑动屏幕
     */
    val isSeeking: Boolean by derivedStateOf {
        !seekDelta.isNaN()
    }

    /**
     * 当前正在快进的秒数.
     *
     * 当用户手指在屏幕上滑动时, [deltaSeconds] 将更新, 反映假如用户此时松开手指, 将会跳转的秒数.
     * - 若用户从屏幕左边滑到屏幕的右边, [deltaSeconds] 将会是 [SwipeSeekerConfig.maxDragSeconds].
     *
     * 当未在滑动时, [deltaSeconds] 为 `0`.
     *
     * 负数表示快退, 正数表示快进
     */
    val deltaSeconds: Int by derivedStateOf {
        if (seekDelta.isNaN()) {
            0
        } else {
            // draggable 上报的累计位移不含手势识别时扣除的 touch slop, 补回后秒数与
            // 手指相对按下点的位移严格对应, 避免快进/快退看起来"不跟手".
            val compensated = seekDelta + touchSlopPx * seekDelta.sign
            val percentage = compensated / screenWidthPx
            (percentage * swipeSeekerConfig.maxDragSeconds).roundToInt()
        }
    }


    companion object {
        fun Modifier.swipeToSeek(
            seekerState: SwipeSeekerState,
            orientation: Orientation,
            enabled: Boolean = true,
            interactionSource: MutableInteractionSource? = null,
            reverseDirection: Boolean = false,
            onDragStarted: suspend CoroutineScope.(startedPosition: Offset) -> Unit = {},
            onDragStopped: suspend CoroutineScope.(velocity: Float, cancelled: Boolean) -> Unit = { _, _ -> },
            onCancellationChanged: (cancelled: Boolean) -> Unit = {},
            onDelta: (Float) -> Unit = {},
        ): Modifier {
            return composed(
                inspectorInfo = {
                    name = "videoSeeker"
                    properties["seekerState"] = seekerState
                },
            ) {
                draggable(
                    rememberDraggableState {
                        seekerState.onSwipeOffset(it)
                        onDelta(it)
                    },
                    orientation,
                    onDragStarted = {
                        seekerState.onSwipeStarted()
                        onDragStarted(it)
                    },
                    onDragStopped = {
                        val cancelled = seekerState.isCancelled
                        seekerState.onSwipeStopped()
                        onDragStopped(it, cancelled)
                    },
                    enabled = enabled,
                    interactionSource = interactionSource,
                    reverseDirection = reverseDirection,
                ).trackSwipeSeekCancellation(seekerState, onCancellationChanged)
            }
        }
    }
}
