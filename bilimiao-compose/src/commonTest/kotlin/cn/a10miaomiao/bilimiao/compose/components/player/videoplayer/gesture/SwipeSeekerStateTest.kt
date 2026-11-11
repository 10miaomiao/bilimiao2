package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * [SwipeSeekerState] 的手势判定测试.
 *
 * 只覆盖不需要 Composition 的纯逻辑: 主轴判定、向上滑动取消、松手时提交/放弃快进.
 */
class SwipeSeekerStateTest {

    private companion object {
        private const val SCREEN_WIDTH_PX = 1000
        private const val TOUCH_SLOP_PX = 24f
    }

    private val seekedSeconds = mutableListOf<Int>()

    private fun createState(
        config: SwipeSeekerConfig = SwipeSeekerConfig.Default,
    ) = SwipeSeekerState(
        screenWidthPx = SCREEN_WIDTH_PX,
        swipeSeekerConfig = config,
        density = Density(1f),
        touchSlopPx = TOUCH_SLOP_PX,
        onSeek = { seekedSeconds += it },
    )

    @Test
    fun verticalDominantDragIsDetected() {
        val start = Offset(100f, 500f)
        // 竖直 200px, 水平 20px
        assertTrue(isVerticalDominantDrag(start.x, start.y, Offset(120f, 700f)))
        // 向下滑同样算竖直为主
        assertTrue(isVerticalDominantDrag(start.x, start.y, Offset(80f, 300f)))
    }

    @Test
    fun horizontalDragIsNotVerticalDominant() {
        val start = Offset(100f, 500f)
        assertFalse(isVerticalDominantDrag(start.x, start.y, Offset(400f, 520f)))
        // 完全水平
        assertFalse(isVerticalDominantDrag(start.x, start.y, Offset(300f, 500f)))
    }

    @Test
    fun unspecifiedPositionIsNotVerticalDominant() {
        assertFalse(isVerticalDominantDrag(Float.NaN, 500f, Offset(120f, 700f)))
        assertFalse(isVerticalDominantDrag(100f, Float.NaN, Offset(120f, 700f)))
        assertFalse(isVerticalDominantDrag(100f, 500f, Offset.Unspecified))
    }

    @Test
    fun upwardDragOverCancelDistanceCancelsSeek() {
        val dragStartY = 600f
        // 上移 145px > 144px
        assertTrue(isVerticalDragCancelled(dragStartY, Offset(120f, 455f), 144f))
        // 恰好 144px 不取消
        assertFalse(isVerticalDragCancelled(dragStartY, Offset(120f, 456f), 144f))
        // 向下滑不取消
        assertFalse(isVerticalDragCancelled(dragStartY, Offset(120f, 900f), 144f))
    }

    @Test
    fun notSeekingWhenPointerJustDown() {
        val state = createState()
        state.onPointerDown(Offset(100f, 500f))
        assertFalse(state.isSeeking)
        assertEquals(0, state.deltaSeconds)
        // 没开始滑动就松手不应回调
        state.onSwipeStopped()
        assertTrue(seekedSeconds.isEmpty())
    }

    @Test
    fun horizontalSwipeCommitsSeekWithTouchSlopCompensation() {
        val state = createState()
        state.onPointerDown(Offset(100f, 500f))
        state.onSwipeStarted()
        state.onSwipeOffset(120f)
        state.updateCancellation(Offset(220f, 506f))
        assertFalse(state.isCancelled)
        // (120 + 24) / 1000 * 97 = 13.968 -> 四舍五入 14 秒
        assertEquals(14, state.deltaSeconds)
        state.onSwipeStopped()
        assertEquals(listOf(14), seekedSeconds)
        assertFalse(state.isSeeking)
    }

    @Test
    fun verticalSwipeWithHorizontalJitterDoesNotCommitSeek() {
        val state = createState()
        state.onPointerDown(Offset(100f, 500f))
        // 竖直滑动时 draggable(Horizontal) 也会累计一点水平抖动
        state.onSwipeStarted()
        state.onSwipeOffset(35f)
        assertTrue(state.updateCancellation(Offset(135f, 700f)))
        assertTrue(state.isCancelled)
        state.onSwipeStopped()
        assertTrue(seekedSeconds.isEmpty())
        assertFalse(state.isCancelled)
        assertFalse(state.isSeeking)
    }

    @Test
    fun cancellationIsRestoredWhenFingerMovesBack() {
        val state = createState()
        state.onPointerDown(Offset(100f, 600f))
        state.onSwipeStarted()
        state.onSwipeOffset(50f)
        // 上移 200px 超过取消距离
        assertTrue(state.updateCancellation(Offset(115f, 400f)))
        assertTrue(state.isCancelled)
        // 滑回基准附近, 且转为水平为主
        assertTrue(state.updateCancellation(Offset(160f, 590f)))
        assertFalse(state.isCancelled)
        state.onSwipeStopped()
        assertEquals(1, seekedSeconds.size)
    }

    @Test
    fun backwardsSwipeReportsNegativeSeconds() {
        val state = createState()
        state.onPointerDown(Offset(500f, 500f))
        state.onSwipeStarted()
        state.onSwipeOffset(-120f)
        state.updateCancellation(Offset(380f, 504f))
        assertFalse(state.isCancelled)
        assertTrue(state.deltaSeconds < 0)
        state.onSwipeStopped()
        assertEquals(1, seekedSeconds.size)
        assertTrue(seekedSeconds.single() < 0)
    }
}
