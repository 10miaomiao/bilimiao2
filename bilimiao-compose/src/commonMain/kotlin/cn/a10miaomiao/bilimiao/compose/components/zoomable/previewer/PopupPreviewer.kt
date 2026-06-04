package cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer

import androidx.annotation.IntRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.DEFAULT_ITEM_SPACE
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.PagerGestureScope
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.PagerZoomablePolicyScope
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.SupportedPagerState
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.ZoomablePager
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.ZoomablePagerState
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.rememberSupportedPagerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * @program: PopupPreviewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-12-13 11:45
 **/

/**
 * 默认的弹出预览时的动画效果
 */
val DEFAULT_PREVIEWER_ENTER_TRANSITION =
    scaleIn(tween(180)) + fadeIn(tween(240))

/**
 * 默认的关闭预览时的动画效果
 */
val DEFAULT_PREVIEWER_EXIT_TRANSITION =
    scaleOut(tween(320)) + fadeOut(tween(240))

/**
 * Compose中获取一个PopupPreviewerState的方式
 *
 * @param initialPage 初始页码
 * @param pageCount 总页数
 * @return 返回一个PopupPreviewerState
 */
@Composable
fun rememberPopupPreviewerState(
    @IntRange(from = 0) initialPage: Int = 0,
    pageCount: () -> Int,
): PopupPreviewerState {
    val pagerState = rememberSupportedPagerState(initialPage, pageCount)
    return remember {
        PopupPreviewerState(pagerState = pagerState)
    }
}

/**
 * 弹出预览状态与控制对象
 *
 * @constructor
 *
 * @param pagerState 封装的通用pagerState
 */
open class PopupPreviewerState(
    pagerState: SupportedPagerState,
) : ZoomablePagerState(pagerState) {

    // 锁对象
    private var mutex = Mutex()

    // 最外侧animateVisibleState
    internal var animateContainerVisibleState by mutableStateOf(MutableTransitionState(false))

    // 用于监听状态
    internal var containerVisibleFlow = MutableStateFlow(false)

    // 进入转换动画
    internal var enterTransition: EnterTransition? = null

    // 离开的转换动画
    internal var exitTransition: ExitTransition? = null

    // 标记打开动作，执行开始
    internal suspend fun stateOpenStart() =
        updateState(animating = true, visible = false, visibleTarget = true)

    // 标记打开动作，执行结束
    internal suspend fun stateOpenEnd() =
        updateState(animating = false, visible = true, visibleTarget = null)

    // 标记关闭动作，执行开始
    internal suspend fun stateCloseStart() =
        updateState(animating = true, visible = true, visibleTarget = false)

    // 标记关闭动作，执行结束
    internal suspend fun stateCloseEnd() =
        updateState(animating = false, visible = false, visibleTarget = null)

    // 是否正在进行动画
    var animating by mutableStateOf(false)
        internal set

    // 是否可见
    var visible by mutableStateOf(false)
        internal set

    // 是否可见的目标值
    var visibleTarget by mutableStateOf<Boolean?>(null)
        internal set

    // 是否允许执行open操作
    val canOpen: Boolean
        get() = !visible && visibleTarget == null && !animating

    // 是否允许执行close操作
    val canClose: Boolean
        get() = visible && visibleTarget == null && !animating

    /**
     * 更新当前的标记状态
     * @param animating Boolean
     * @param visible Boolean
     * @param visibleTarget Boolean?
     */
    suspend fun updateState(animating: Boolean, visible: Boolean, visibleTarget: Boolean?) {
        mutex.withLock {
            this.animating = animating
            this.visible = visible
            this.visibleTarget = visibleTarget
        }
    }

    open suspend fun openAction(
        index: Int = 0,
        enterTransition: EnterTransition? = null,
    ) {
        // 设置当前转换动画
        this.enterTransition = enterTransition
        // container动画立即设置为关闭
        animateContainerVisibleState = MutableTransitionState(false)
        // 开启container
        animateContainerVisibleState.targetState = true
        // 滚动到指定页面
        scrollToPage(index)
        // 等状态变到目标值
        containerVisibleFlow.takeWhile { !it }.collect {}
    }

    suspend fun open(
        index: Int = 0,
        enterTransition: EnterTransition? = null,
    ) {
        // 标记状态
        stateOpenStart()
        // 实际业务发生
        openAction(index, enterTransition)
        // 标记状态
        stateOpenEnd()
    }

    open suspend fun closeAction(
        exitTransition: ExitTransition? = null,
    ) {
        // 设置当前转换动画
        this.exitTransition = exitTransition
        // 这里创建一个全新的state是为了让exitTransition的设置得到响应
        animateContainerVisibleState = MutableTransitionState(true)
        // 开启container关闭动画
        animateContainerVisibleState.targetState = false
        // 等状态变到目标值
        containerVisibleFlow.takeWhile { it }.collect {}
    }

    suspend fun close(
        exitTransition: ExitTransition? = null,
    ) {
        // 标记状态
        stateCloseStart()
        // 实际业务发生
        closeAction(exitTransition)
        // 标记状态
        stateCloseEnd()
    }
}

/**
 * 基于Pager、ZoomableView实现的弹出预览组件
 *
 * @param modifier 图层修饰
 * @param state 组件的状态与控制对象
 * @param itemSpacing 图片间的间隔
 * @param beyondViewportPageCount 页面外缓存个数
 * @param enter 进入动画
 * @param exit 退出动画
 * @param detectGesture 检测手势
 * @param previewerDecoration 外侧图层容器修饰
 * @param zoomablePolicy ZoomableView图层修饰
 */
@Composable
fun PopupPreviewer(
    modifier: Modifier = Modifier,
    state: PopupPreviewerState,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    beyondViewportPageCount: Int = DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT,
    enter: EnterTransition = DEFAULT_PREVIEWER_ENTER_TRANSITION,
    exit: ExitTransition = DEFAULT_PREVIEWER_EXIT_TRANSITION,
    detectGesture: PagerGestureScope = PagerGestureScope(),
    previewerDecoration: @Composable (innerBox: @Composable () -> Unit) -> Unit =
        @Composable { innerBox -> innerBox() },
    zoomablePolicy: @Composable PagerZoomablePolicyScope.(page: Int) -> Unit,
) {
    state.apply {
        LaunchedEffect(animateContainerVisibleState.currentState) {
            containerVisibleFlow.value = animateContainerVisibleState.currentState
        }
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visibleState = animateContainerVisibleState,
            enter = enterTransition ?: enter,
            exit = exitTransition ?: exit,
        ) {
            previewerDecoration {
                ZoomablePager(
                    modifier = modifier.fillMaxSize(),
                    state = state,
                    itemSpacing = itemSpacing,
                    beyondViewportPageCount = beyondViewportPageCount,
                    detectGesture = detectGesture,
                    zoomablePolicy = zoomablePolicy,
                    // 正在动画中不允许页面滚动
                    userScrollEnabled = !animating
                )
            }
        }
    }
}