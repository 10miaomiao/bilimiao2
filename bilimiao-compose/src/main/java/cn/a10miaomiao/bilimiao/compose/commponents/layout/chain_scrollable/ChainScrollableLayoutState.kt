package cn.a10miaomiao.bilimiao.compose.commponents.layout.chain_scrollable

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun rememberChainScrollableLayoutState(
    maxScrollPosition: Dp,
): ChainScrollableLayoutState {
    val density = LocalDensity.current
    val maxPx = remember(key1 = maxScrollPosition, key2 = density) {
        density.run { maxScrollPosition.toPx() }
    }
    val offsetY = rememberSaveable() {
        mutableStateOf(0f)
    }
    val coroutineScope = rememberCoroutineScope()
    return remember(coroutineScope, maxPx, offsetY) {
        ChainScrollableLayoutState(
            coroutineScope,
            offsetY,
            maxPx,
        )
    }
}

@Stable
class ChainScrollableLayoutState(
    private val coroutineScope: CoroutineScope,
    private val offsetYState: MutableState<Float>,
    val maxPx: Float,
) {
    private val offsetY = Animatable(offsetYState.value)

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
}