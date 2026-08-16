@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 在屏幕上垂直滑动以进入/退出全屏.
 *
 * 滑动距离超过 [swipeThreshold] 时立即触发, 每次手势只触发一次:
 * - 向上滑动: [onEnterFullscreen]
 * - 向下滑动: [onExitFullscreen]
 */
fun Modifier.swipeToFullscreen(
    enabled: Boolean = true,
    swipeThreshold: Dp = 64.dp,
    onEnterFullscreen: () -> Unit,
    onExitFullscreen: () -> Unit,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "swipeToFullscreen"
        properties["swipeThreshold"] = swipeThreshold
    },
) {
    val onEnterFullscreenState by rememberUpdatedState(onEnterFullscreen)
    val onExitFullscreenState by rememberUpdatedState(onExitFullscreen)
    val thresholdPx = with(LocalDensity.current) { swipeThreshold.toPx() }
    var totalDelta by remember { mutableFloatStateOf(0f) }
    var triggered by remember { mutableStateOf(false) }
    draggable(
        rememberDraggableState { delta ->
            if (triggered) return@rememberDraggableState
            totalDelta += delta
            if (totalDelta <= -thresholdPx) {
                // 向上滑动
                triggered = true
                onEnterFullscreenState()
            } else if (totalDelta >= thresholdPx) {
                // 向下滑动
                triggered = true
                onExitFullscreenState()
            }
        },
        Orientation.Vertical,
        enabled = enabled,
        onDragStarted = {
            totalDelta = 0f
            triggered = false
        },
    )
}
