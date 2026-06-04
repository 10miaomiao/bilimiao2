package cn.a10miaomiao.bilimiao.compose.components.zoomable.pager

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.components.zoomable.ZoomableGestureScope
import cn.a10miaomiao.bilimiao.compose.components.zoomable.ZoomableView
import cn.a10miaomiao.bilimiao.compose.components.zoomable.ZoomableViewState
import cn.a10miaomiao.bilimiao.compose.components.zoomable.rememberZoomableState
import kotlinx.coroutines.launch

/**
 * @program: ZoomablePager
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-12-06 20:48
 **/

// 图片间的默认间隔
val DEFAULT_ITEM_SPACE = 12.dp

// 页面外缓存个数
const val DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT = 1

/**
 * 在ZoomablePager中对ZoomableView图层进行修饰对对象
 *
 */
fun interface PagerZoomablePolicyScope {
    @Composable
    fun ZoomablePolicy(
        intrinsicSize: Size,
        content: @Composable (ZoomableViewState) -> Unit,
    )
}

/**
 * 用于获取ZoomablePager的状态和对其进行控制
 *
 * @property pagerState 底层SupportedPagerState
 */
open class ZoomablePagerState(
    val pagerState: SupportedPagerState,
) {

    /**
     * 当前viewer的状态
     */
    val zoomableViewState = mutableStateOf<ZoomableViewState?>(null)

    /**
     * 当前页码
     */
    val currentPage: Int
        get() = pagerState.currentPage

    /**
     * 目标页码
     */
    val targetPage: Int
        get() = pagerState.targetPage

    /**
     * interactionSource
     */
    val interactionSource: InteractionSource
        get() = pagerState.interactionSource

    /**
     * 滚动到指定页面
     * @param page Int
     * @param pageOffset Float
     */
    suspend fun scrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) = pagerState.scrollToPage(page, pageOffset)

    /**
     * 动画滚动到指定页面
     * @param page Int
     * @param pageOffset Float
     */
    suspend fun animateScrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) = pagerState.animateScrollToPage(page, pageOffset)

}

/**
 * 在Compose中获取一个ZoomablePagerState
 *
 * @param initialPage 初始页码
 * @param pageCount 总页数
 * @return
 */
@Composable
fun rememberZoomablePagerState(
    @IntRange(from = 0) initialPage: Int = 0,
    pageCount: () -> Int,
): ZoomablePagerState {
    val zoomablePagerState = rememberSupportedPagerState(initialPage, pageCount)
    return remember { ZoomablePagerState(zoomablePagerState) }
}

/**
 * Pager的点击事件监听对象
 *
 * @property onTap 点击事件
 * @property onDoubleTap 双击事件
 * @property onLongPress 长按事件
 */
class PagerGestureScope(
    var onTap: () -> Unit = {},
    var onDoubleTap: () -> Boolean = { false },
    var onLongPress: () -> Unit = {},
)

/**
 * 基于Pager和ZoomableView实现的一个图片查看列表组件
 *
 * @param modifier 图层修饰
 * @param state pager状态获取与控制
 * @param itemSpacing 每张图片之间的间隔
 * @param beyondViewportPageCount 页面外缓存个数
 * @param userScrollEnabled 是否允许页面滚动
 * @param detectGesture 检测手势
 * @param zoomablePolicy 图层本体
 */
@Composable
fun ZoomablePager(
    modifier: Modifier = Modifier,
    state: ZoomablePagerState,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    beyondViewportPageCount: Int = DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT,
    userScrollEnabled: Boolean = true,
    detectGesture: PagerGestureScope = PagerGestureScope(),
    zoomablePolicy: @Composable PagerZoomablePolicyScope.(page: Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    // 确保不会越界
    SupportedHorizonPager(
        state = state.pagerState,
        modifier = modifier
            .fillMaxSize(),
        itemSpacing = itemSpacing,
        beyondViewportPageCount = beyondViewportPageCount,
        userScrollEnabled = userScrollEnabled,
    ) { page ->
        Box(modifier = Modifier.fillMaxSize()) {
            PagerZoomablePolicyScope { intrinsicSize, content ->
                val zoomableState = rememberZoomableState(contentSize = intrinsicSize)
                LaunchedEffect(key1 = state.currentPage, key2 = zoomableState) {
                    if (state.currentPage == page) {
                        state.zoomableViewState.value = zoomableState
                    } else {
                        zoomableState.reset()
                    }
                }
                ZoomableView(
                    state = zoomableState,
                    boundClip = false,
                    detectGesture = ZoomableGestureScope(
                        onTap = { detectGesture.onTap() },
                        onDoubleTap = {
                            val consumed = detectGesture.onDoubleTap()
                            if (!consumed) scope.launch {
                                zoomableState.toggleScale(it)
                            }
                        },
                        onLongPress = { detectGesture.onLongPress() },
                    )
                ) {
                    content(zoomableState)
                }
            }.zoomablePolicy(page)
        }
    }
}