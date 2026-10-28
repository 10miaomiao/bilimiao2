package cn.a10miaomiao.bilimiao.compose.components.layout.chain_scrollable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    // 状态对象本身保持稳定，只在 density 变化时重建，
    // 否则头部高度动画（如视频详情页封面显示/隐藏）会让整棵子树反复重建状态
    val state = remember(density) {
        ChainScrollableLayoutState(heightOffset, density)
    }
    // 头部高度可能在动画中持续变化，这里同步刷新可折叠范围，
    // 让布局立即跟随新的高度，而不是等下一次滚动才校正
    state.updateScrollRange(maxScrollPosition, minScrollPosition)
    return state
}

@Stable
class ChainScrollableLayoutState(
    private val heightOffsetState: MutableState<Float>,
    val density: Density,
) {
    private val maxScrollPositionState = mutableStateOf(0.dp)
    private val minScrollPositionState = mutableStateOf(0.dp)

    /**
     * 完全折叠时头部保留的高度（通常是状态栏高度）
     */
    val maxScrollPosition: Dp get() = maxScrollPositionState.value

    /**
     * 头部最少可见的高度
     */
    val minScrollPosition: Dp get() = minScrollPositionState.value

    val maxPx: Float get() = density.run { maxScrollPosition.toPx() }
    val minPx: Float get() = density.run { minScrollPosition.toPx() }

    /**
     * 可折叠的总高度（px）
     */
    val maxCollapsiblePx: Float get() = (maxPx - minPx).coerceAtLeast(0f)

    /**
     * 头部可见比例（px 无关）
     * 1 = 完全展开，0 = 完全折叠
     */
    val headerFraction: Float
        get() {
            val collapsible = maxCollapsiblePx
            if (collapsible <= 0f) return 1f
            return (1f - getHeightOffset() / collapsible).coerceIn(0f, 1f)
        }

    /**
     * 同步可折叠范围。
     * 头部高度变化时，需要把已有的折叠偏移量收敛到新的范围内，
     * 否则会出现内容错位（偏移量比可折叠高度还大）。
     */
    fun updateScrollRange(maxScrollPosition: Dp, minScrollPosition: Dp) {
        if (this.maxScrollPosition != maxScrollPosition) {
            maxScrollPositionState.value = maxScrollPosition
        }
        if (this.minScrollPosition != minScrollPosition) {
            minScrollPositionState.value = minScrollPosition
        }
        val current = heightOffsetState.value
        val clamped = current.coerceIn(0f, maxCollapsiblePx)
        if (clamped != current) {
            heightOffsetState.value = clamped
        }
    }

    /**
     * 当前头部折叠偏移量（px）
     * 0 = 完全展开，maxCollapsiblePx = 完全折叠
     */
    fun getHeightOffset(): Float =
        heightOffsetState.value.coerceIn(0f, maxCollapsiblePx)

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
