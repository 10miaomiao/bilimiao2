package cn.a10miaomiao.bilimiao.compose.components.layout.chain_scrollable

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun rememberChainScrollableLayoutState(
    maxScrollPosition: Dp,
    minScrollPosition: Dp = 0.dp,
): ChainScrollableLayoutState {
    val density = LocalDensity.current
    val offsetY = rememberSaveable() {
        mutableFloatStateOf(0f)
    }
    val coroutineScope = rememberCoroutineScope()
    return remember(coroutineScope, maxScrollPosition, minScrollPosition, density) {
        ChainScrollableLayoutState(
            coroutineScope,
            offsetY,
            maxScrollPosition,
            minScrollPosition,
            density,
        )
    }
}

@Stable
class ChainScrollableLayoutState(
    private val coroutineScope: CoroutineScope,
    private val offsetYState: MutableState<Float>,
    val maxScrollPosition: Dp,
    val minScrollPosition: Dp,
    val density: Density,
) {
    private val offsetY = Animatable(offsetYState.value)

    val maxPx = density.run { maxScrollPosition.toPx() }
    val minPx = density.run { minScrollPosition.toPx() }

    fun getOffsetYValue(): Float = offsetY.value

    /**
     * 修改滚动的位置
     * Set number of scroll position
     */
    fun setOffsetY(value: Float) {
        coroutineScope.launch {
            offsetY.snapTo(value)
            offsetYState.value = value
        }
    }

    suspend fun scrollToMax() {
        offsetY.snapTo(-maxPx)
        offsetYState.value = -maxPx
    }

    val nestedScroll = object : NestedScrollConnection {
        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            if (
                available.y > 0f
                && getOffsetYValue() < 0f
            ) {
                setOffsetY(minOf(getOffsetYValue() + available.y, 0f))
                return available
            }
            return Offset.Zero
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            return super.onPostScroll(consumed, available, source)
        }
    }

}