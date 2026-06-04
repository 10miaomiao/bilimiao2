package cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer

import androidx.annotation.IntRange
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.DEFAULT_ITEM_SPACE
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.PagerGestureScope
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.PagerZoomablePolicyScope
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.SupportedPagerState
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.rememberSupportedPagerState
import kotlinx.coroutines.CoroutineScope

/**
 * 获取一个图片预览的状态与控制对象
 *
 * @param scope 协程作用域
 * @param defaultAnimationSpec 默认动画窗格
 * @param initialPage 初始化页码
 * @param verticalDragType 垂直手势类型
 * @param pageCount 总页数
 * @param getKey 获取某一页的key的方法
 * @return 返回一个PreviewerState
 */
@Composable
fun rememberPreviewerState(
    scope: CoroutineScope = rememberCoroutineScope(),
    defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC,
    @IntRange(from = 0) initialPage: Int = 0,
    verticalDragType: VerticalDragType = VerticalDragType.Down,
    transformItemStateMap: ItemStateMap = LocalTransformItemStateMap.current,
    pageCount: () -> Int,
    getKey: (Int) -> Any = {},
): PreviewerState {
    val pagerState = rememberSupportedPagerState(initialPage = initialPage, pageCount = pageCount)
    val previewerState = remember {
        PreviewerState(
            scope = scope,
            verticalDragType = verticalDragType,
            pagerState = pagerState,
            itemStateMap = transformItemStateMap,
            getKey = getKey,
        )
    }
    previewerState.defaultAnimationSpec = defaultAnimationSpec
    return previewerState
}

/**
 * 图片预览的状态与控制对象
 *
 * @constructor
 *
 * @param scope 协程作用域
 * @param defaultAnimationSpec 默认动画窗格
 * @param verticalDragType 垂直手势类型
 * @param scaleToCloseMinValue 下拉关闭的缩小的阈值
 * @param pagerState 预览状态
 * @param itemStateMap 用于获取transformItemState
 * @param getKey 获取当前key的方法
 */
class PreviewerState(
    scope: CoroutineScope,
    defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC,
    verticalDragType: VerticalDragType = VerticalDragType.None,
    scaleToCloseMinValue: Float = DEFAULT_SCALE_TO_CLOSE_MIN_VALUE,
    pagerState: SupportedPagerState,
    itemStateMap: ItemStateMap,
    getKey: (Int) -> Any,
) : DraggablePreviewerState(
    scope,
    defaultAnimationSpec,
    verticalDragType,
    scaleToCloseMinValue,
    pagerState,
    itemStateMap,
    getKey
)

/**
 * 带转换效果的图片弹出预览组件
 *
 * @param modifier 图层修饰
 * @param state 状态对象
 * @param itemSpacing 图片间的间隔
 * @param beyondViewportPageCount 页面外缓存个数
 * @param enter 调用open时的进入动画
 * @param exit 调用close时的退出动画
 * @param debugMode 调试模式
 * @param detectGesture 检测手势
 * @param previewerLayer 容器的图层修饰
 * @param zoomablePolicy 缩放图层的修饰
 */
@Composable
fun Previewer(
    // 编辑参数
    modifier: Modifier = Modifier,
    // 内容边距
    contentPadding: PaddingValues = PaddingValues(),
    // 状态对象
    state: PreviewerState,
    // 图片间的间隔
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    // 页面外缓存个数
    beyondViewportPageCount: Int = DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT,
    // 进入动画
    enter: EnterTransition = DEFAULT_PREVIEWER_ENTER_TRANSITION,
    // 退出动画
    exit: ExitTransition = DEFAULT_PREVIEWER_EXIT_TRANSITION,
    // 调试模式
    debugMode: Boolean = false,
    // 检测手势
    detectGesture: PagerGestureScope = PagerGestureScope(),
    // 图层修饰
    previewerLayer: TransformLayerScope = TransformLayerScope(),
    // 缩放图层
    zoomablePolicy: @Composable PagerZoomablePolicyScope.(page: Int) -> Boolean,
) {
    DraggablePreviewer(
        modifier = modifier,
        contentPadding = contentPadding,
        state = state,
        itemSpacing = itemSpacing,
        beyondViewportPageCount = beyondViewportPageCount,
        enter = enter,
        exit = exit,
        debugMode = debugMode,
        detectGesture = detectGesture,
        previewerLayer = previewerLayer,
        zoomablePolicy = zoomablePolicy,
    )
}