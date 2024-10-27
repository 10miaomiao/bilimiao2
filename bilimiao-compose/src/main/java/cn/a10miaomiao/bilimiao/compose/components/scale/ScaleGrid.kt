package cn.a10miaomiao.bilimiao.compose.components.scale

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

/**
 * @program: UICommon
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-06 00:18
 **/

const val DEFAULT_GRID_SCALE_SIZE = 0.84F
const val DEFAULT_LONG_PRESS_TIME = 400L

class DetectScaleGridGesture(
    var onPress: () -> Unit = {},
    var onLongPress: () -> Unit = {},
)

@Composable
fun ScaleGrid(
    modifier: Modifier = Modifier,
    scaleSize: Float = DEFAULT_GRID_SCALE_SIZE,
    longPressTime: Long = DEFAULT_LONG_PRESS_TIME,
    detectGesture: DetectScaleGridGesture? = null,
    content: @Composable (Float) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val itemScale = remember { Animatable(1F) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = itemScale.value
                scaleY = itemScale.value
            }
            .pointerInput(detectGesture) {
                awaitEachGesture {
                    awaitFirstDown()
                    // 这里开始
                    scope.launch {
                        itemScale.animateTo(scaleSize)
                    }
                    try {
                        withTimeout(longPressTime) {
                            var move = false
                            do {
                                val event = awaitPointerEvent()
                                if (!move) {
                                    move = event.type == PointerEventType.Move
                                    break
                                }
                            } while (event.changes.any { it.pressed })
                            if (!move) {
                                detectGesture?.onPress?.invoke()
                            }
                        }
                    } catch (e: Exception) {
                        detectGesture?.onLongPress?.invoke()
                    }
                    // 这里结束
                    scope.launch {
                        itemScale.animateTo(1F)
                    }
                }
            }
    ) {
        content(itemScale.value)
    }
}