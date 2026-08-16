@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.annotation.MainThread
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope

/**
 * 等级控制器, 用于控制音量/亮度等具有上下限的连续值.
 *
 * 原始 animeko 实现中的 `AudioManager`/`BrightnessManager` 相关扩展已移除,
 * 调用方需自行实现该接口以对接具体平台能力.
 */
interface LevelController {
    val level: Float

    val range: ClosedRange<Float>

    /** 该控制器能表示的最小等级变化. */
    val levelStep: Float get() = 0.01f

    @MainThread
    fun setLevel(level: Float)
}

object NoOpLevelController : LevelController {
    override val level: Float
        get() = 0f

    override val range: ClosedRange<Float> = 0f..1f

    override fun setLevel(level: Float) {

    }
}

@MainThread
fun LevelController.increaseLevel(step: Float = 0.05f) {
    setLevel((level + step).coerceAtMost(range.endInclusive))
}

@MainThread
fun LevelController.decreaseLevel(step: Float = 0.05f) {
    setLevel((level - step).coerceAtLeast(range.start))
}

fun Modifier.swipeLevelControlWithIndicator(
    controller: LevelController,
    stepSize: Dp,
    orientation: Orientation,
    indicatorState: GestureIndicatorState,
    enabled: Boolean = true,
    step: Float = 0.05f,
    setup: () -> Unit = {}
): Modifier = this then swipeLevelControl(
    controller = controller,
    stepSize = stepSize,
    orientation = orientation,
    step = step,
    enabled = enabled,
    afterStep = {
        setup()
        indicatorState.progressValue = controller.level
    },
    onDragStarted = {
        indicatorState.visible = true
    },
    onDragStopped = {
        indicatorState.visible = false
    },
)

fun Modifier.swipeLevelControl(
    controller: LevelController,
    stepSize: Dp,
    orientation: Orientation,
    step: Float = 0.05f,
    enabled: Boolean = true,
    afterStep: (StepDirection) -> Unit = {},
    onDragStarted: suspend CoroutineScope.(startedPosition: Offset) -> Unit = {},
    onDragStopped: suspend CoroutineScope.(velocity: Float) -> Unit = {},
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "swipeLevelControl"
        properties["controller"] = controller
        properties["stepSize"] = stepSize
        properties["orientation"] = orientation
    },
) {
    steppedDraggable(
        rememberSteppedDraggableState(
            stepSize = stepSize,
            onStep = { direction ->
                when (direction) {
                    StepDirection.FORWARD -> controller.increaseLevel(step)
                    StepDirection.BACKWARD -> controller.decreaseLevel(step)
                }
                afterStep(direction)
            },
        ),
        orientation = orientation,
        enabled = enabled,
        onDragStarted = onDragStarted,
        onDragStopped = onDragStopped,
    )

}
