package cn.a10miaomiao.bilimiao.compose.components.layout

import cn.a10miaomiao.bilimiao.compose.StartViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * [deriveDrawerWrapperState] 的纯逻辑测试.
 *
 * 覆盖点:
 * 1. 动画进行中且朝展开去时一律 SETTLING;
 * 2. 手势松手后停在收紧锚点（没有任何后续位移帧）必须收敛到 COLLAPSED——这是
 *    「关掉抽屉后按返回键要多按一次」的回归场景;
 * 3. 完全展开收敛到 EXPANDED;
 * 4. 拖拽中（半开）判为 DRAGGING，即仍算打开，返回键要先关抽屉;
 * 5. 几何未测量（openFraction = NaN）时只按 settled 判定;
 * 6. 朝关闭的过渡动画直接算已收起（程序化关闭期间返回键不被静默吞掉）;
 * 7. [StartViewState] 侧契约：只有 COLLAPSED 时 isDrawerOpen() 为 false.
 */
class DrawerWrapperStateTest {

    @Test
    fun animationRunningIsSettling() {
        // 朝打开去的动画：无论是否已吸附到锚点、可见比例是多少，都是 SETTLING
        assertEquals(
            StartViewState.DRAWER_STATE_SETTLING,
            deriveDrawerWrapperState(
                settledOpen = true,
                targetOpen = true,
                openFraction = 1f,
                isAnimationRunning = true,
            ),
        )
        assertEquals(
            StartViewState.DRAWER_STATE_SETTLING,
            deriveDrawerWrapperState(
                settledOpen = false,
                targetOpen = true,
                openFraction = 0f,
                isAnimationRunning = true,
            ),
        )
        assertEquals(
            StartViewState.DRAWER_STATE_SETTLING,
            deriveDrawerWrapperState(
                settledOpen = true,
                targetOpen = true,
                openFraction = 0.5f,
                isAnimationRunning = true,
            ),
        )
    }

    @Test
    fun gestureClosedAtAnchorIsCollapsed() {
        // 核心回归：松手后 offset 已落在 Closed 锚点、之后没有位移帧，
        // 只有 settled 从 Open 变 Closed，必须收敛到 COLLAPSED 而不是残留 DRAGGING
        assertEquals(
            StartViewState.DRAWER_STATE_COLLAPSED,
            deriveDrawerWrapperState(
                settledOpen = false,
                targetOpen = false,
                openFraction = 0f,
                isAnimationRunning = false,
            ),
        )
    }

    @Test
    fun fullyExpandedIsExpanded() {
        assertEquals(
            StartViewState.DRAWER_STATE_EXPANDED,
            deriveDrawerWrapperState(
                settledOpen = true,
                targetOpen = true,
                openFraction = 1f,
                isAnimationRunning = false,
            ),
        )
    }

    @Test
    fun halfOpenIsDragging() {
        // 半开：无论 settled 落在哪一侧都还在拖拽过程中
        assertEquals(
            StartViewState.DRAWER_STATE_DRAGGING,
            deriveDrawerWrapperState(
                settledOpen = true,
                targetOpen = true,
                openFraction = 0.4f,
                isAnimationRunning = false,
            ),
        )
        assertEquals(
            StartViewState.DRAWER_STATE_DRAGGING,
            deriveDrawerWrapperState(
                settledOpen = false,
                targetOpen = false,
                openFraction = 0.4f,
                isAnimationRunning = false,
            ),
        )
    }

    @Test
    fun unknownFractionFallsBackToSettled() {
        // 几何尚未测量（宽度为 0 / offset 为 NaN）时只按 settled 判定，
        // 不能因为「可见比例为 0」就误判成已收起
        assertEquals(
            StartViewState.DRAWER_STATE_EXPANDED,
            deriveDrawerWrapperState(
                settledOpen = true,
                targetOpen = true,
                openFraction = Float.NaN,
                isAnimationRunning = false,
            ),
        )
        assertEquals(
            StartViewState.DRAWER_STATE_COLLAPSED,
            deriveDrawerWrapperState(
                settledOpen = false,
                targetOpen = false,
                openFraction = Float.NaN,
                isAnimationRunning = false,
            ),
        )
    }

    @Test
    fun animationTowardClosedIsCollapsed() {
        // 回归：程序化关闭动画（requestTarget(Closed) 的 250ms）期间画面还有 60% 可见，
        // 但抽屉正在消失，返回键不该为它让路——否则会静默吞掉一次返回
        assertEquals(
            StartViewState.DRAWER_STATE_COLLAPSED,
            deriveDrawerWrapperState(
                settledOpen = false,
                targetOpen = false,
                openFraction = 0.6f,
                isAnimationRunning = true,
            ),
        )
        // 刚起步（仅剩 20% 可见）同样算已收起
        assertEquals(
            StartViewState.DRAWER_STATE_COLLAPSED,
            deriveDrawerWrapperState(
                settledOpen = false,
                targetOpen = false,
                openFraction = 0.2f,
                isAnimationRunning = true,
            ),
        )
        // 运行时的第一帧实况：settled 要等动画结束才翻成 Closed，此刻仍是 Open + 完全可见
        assertEquals(
            StartViewState.DRAWER_STATE_COLLAPSED,
            deriveDrawerWrapperState(
                settledOpen = true,
                targetOpen = false,
                openFraction = 1f,
                isAnimationRunning = true,
            ),
        )
    }

    @Test
    fun animationTowardOpenKeepsDrawerOpen() {
        // 打开过程仍算打开（SETTLING）：此时返回键应先关抽屉，而不是穿透到页面返回
        assertEquals(
            StartViewState.DRAWER_STATE_SETTLING,
            deriveDrawerWrapperState(
                settledOpen = false,
                targetOpen = true,
                openFraction = 0.2f,
                isAnimationRunning = true,
            ),
        )
    }

    @Test
    fun startViewStateDrawerOpenDependsOnDrawerStateOnly() {
        // PlayerState 的 init 只在注入 scope 上启动一次 DataStore 恢复：传已取消的 scope，
        // 协程体不会执行，因此构造 StartViewState 不依赖平台 DataStore / 调度器
        val startViewState = StartViewState(scope = CoroutineScope(Job().apply { cancel() }))

        // 初始为收起
        assertFalse(startViewState.isDrawerOpen())
        // 拖拽中 / 吸附中 / 已展开都算打开，返回键据此先关抽屉
        startViewState.setDrawerState(StartViewState.DRAWER_STATE_DRAGGING)
        assertTrue(startViewState.isDrawerOpen())
        startViewState.setDrawerState(StartViewState.DRAWER_STATE_SETTLING)
        assertTrue(startViewState.isDrawerOpen())
        startViewState.setDrawerState(StartViewState.DRAWER_STATE_EXPANDED)
        assertTrue(startViewState.isDrawerOpen())
        // 只有收起才算关闭
        startViewState.setDrawerState(StartViewState.DRAWER_STATE_COLLAPSED)
        assertFalse(startViewState.isDrawerOpen())
    }
}
