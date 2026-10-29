package cn.a10miaomiao.bilimiao.compose.common.foundation

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.common.platform.Platform
import cn.a10miaomiao.bilimiao.compose.common.platform.platformInfo

/** 鼠标滚轮一格（scrollDelta.y = 1f）对应的横向滚动距离 */
private val WheelScrollStep = 48.dp

/** 单次滚轮事件的最大滚动距离，避免部分鼠标一个刻度就带多行增量时一次滚太远 */
private val WheelScrollMaxStep = 120.dp

/**
 * 桌面端鼠标滚轮横向滚动适配。
 *
 * 鼠标滚轮的增量只落在 y 轴上（`scrollDelta.x` 恒为 0），而横向列表只认 x 轴增量，
 * 因此桌面上滚动 [androidx.compose.foundation.lazy.LazyRow] 默认没有反应，这里把 y 轴增量换算成横向滚动量。
 *
 * 约定：
 * - 触控板横扫、`Shift` + 滚轮本身就带 x 轴增量，交给 LazyRow 默认逻辑处理，避免重复滚动；
 * - 列表已经滚到该方向的尽头时不消费事件，让滚轮继续作用到外层容器（例如页面的纵向列表）。
 *
 * 用法：`LazyRow(state = state, modifier = Modifier.horizontalWheelScroll(state))`。
 */
@Composable
fun Modifier.horizontalWheelScroll(
    state: LazyListState,
    step: Dp = WheelScrollStep,
): Modifier {
    if (platformInfo.platform != Platform.DESKTOP) return this

    val density = LocalDensity.current
    val stepPx = with(density) { step.toPx() }
    val maxStepPx = with(density) { WheelScrollMaxStep.toPx() }

    return pointerInput(state, stepPx, maxStepPx) {
        awaitPointerEventScope {
            while (true) {
                // Initial 阶段抢在 LazyRow 自身的滚轮逻辑之前拿到事件
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.type != PointerEventType.Scroll) continue

                val change = event.changes.firstOrNull() ?: continue
                val scrollDelta = change.scrollDelta
                if (scrollDelta.x != 0f || scrollDelta.y == 0f) continue

                val canScroll = if (scrollDelta.y > 0f) {
                    state.canScrollForward
                } else {
                    state.canScrollBackward
                }
                if (!canScroll) continue

                change.consume()
                state.dispatchRawDelta((scrollDelta.y * stepPx).coerceIn(-maxStepPx, maxStepPx))
            }
        }
    }
}
