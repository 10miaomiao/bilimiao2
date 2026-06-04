package cn.a10miaomiao.bilimiao.compose.components.layout.chain_scrollable

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun rememberChainScrollableLayoutState(
    maxScrollPosition: Dp,
    minScrollPosition: Dp = 0.dp,
): ChainScrollableLayoutState {
    val density = LocalDensity.current
    val heightOffset = rememberSaveable() {
        mutableFloatStateOf(0f)
    }
    return remember(maxScrollPosition, minScrollPosition, density) {
        ChainScrollableLayoutState(
            heightOffset,
            maxScrollPosition,
            minScrollPosition,
            density,
        )
    }
}

@Stable
class ChainScrollableLayoutState(
    private val heightOffsetState: MutableState<Float>,
    val maxScrollPosition: Dp,
    val minScrollPosition: Dp,
    val density: Density,
) {
    val maxPx = density.run { maxScrollPosition.toPx() }
    val minPx = density.run { minScrollPosition.toPx() }

    /**
     * 可折叠的总高度（px）
     */
    val maxCollapsiblePx: Float get() = maxPx - minPx

    /**
     * 当前头部折叠偏移量（px）
     * 0 = 完全展开，maxCollapsiblePx = 完全折叠
     */
    fun getHeightOffset(): Float = heightOffsetState.value

    fun setHeightOffset(value: Float) {
        heightOffsetState.value = value.coerceIn(0f, maxCollapsiblePx)
    }

    suspend fun scrollToCollapsed() {
        heightOffsetState.value = maxCollapsiblePx
    }

    val nestedScroll = object : NestedScrollConnection {
        /**
         * 在内容滚动之前处理头部的折叠
         * - 向上滑动：优先折叠头部（消费全部滚动量）
         * - 向下滑动：不处理，留给内容区域先滚动
         */
        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            val currentOffset = getHeightOffset()
            // 向上滑动时，优先将头部折叠
            if (available.y < 0 && currentOffset < maxCollapsiblePx) {
                val newOffset = (currentOffset - available.y).coerceAtMost(maxCollapsiblePx)
                setHeightOffset(newOffset)
                return Offset(0f, currentOffset - newOffset)
            }
            return Offset.Zero
        }

        /**
         * 内容滚动后，处理头部展开
         * 向下滑动时只有内容滚到顶部后（available.y > 0 表示内容无法再滚动）才展开头部
         */
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            val currentOffset = getHeightOffset()
            if (available.y > 0 && currentOffset > 0f) {
                val newOffset = (currentOffset - available.y).coerceAtLeast(0f)
                setHeightOffset(newOffset)
                return Offset(0f, currentOffset - newOffset)
            }
            return Offset.Zero
        }
    }

}
