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

    fun getOffsetYValue(): Float = offsetYState.value

    /**
     * 修改滚动的位置
     * Set number of scroll position
     */
    fun setOffsetY(value: Float) {
        offsetYState.value = value
        coroutineScope.launch {
            offsetY.snapTo(value)
        }
    }

    suspend fun scrollToMax() {
        offsetY.snapTo(-maxPx)
        offsetYState.value = -maxPx
    }

    val nestedScroll = object : NestedScrollConnection {
        /**
         * 在内容滚动之前处理头部的折叠和展开
         * 参考 Material 3 TopAppBarScrollBehavior 的实现模式：
         * - 向上滑动：优先折叠头部（消费全部滚动量）
         * - 向下滑动：优先展开头部（只消费头部需要的部分，剩余传给内容）
         */
        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            val currentOffset = getOffsetYValue()
            // 向上滑动时，优先将头部收起
            if (available.y < 0 && currentOffset > minPx - maxPx) {
                val newOffset = maxOf(currentOffset + available.y, minPx - maxPx)
                setOffsetY(newOffset)
                return Offset(0f, newOffset - currentOffset)
            }
            // 向下滑动时，优先将头部展开
            if (available.y > 0 && currentOffset < 0) {
                val consumed = minOf(available.y, -currentOffset)
                setOffsetY(currentOffset + consumed)
                return Offset(0f, consumed)
            }
            return Offset.Zero
        }

        /**
         * 内容滚动后，处理剩余的展开（例如内容滚到顶部后继续下拉展开头部）
         */
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            val currentOffset = getOffsetYValue()
            if (available.y > 0 && currentOffset < 0) {
                setOffsetY(minOf(currentOffset + available.y, 0f))
                return available
            }
            return Offset.Zero
        }
    }

}
