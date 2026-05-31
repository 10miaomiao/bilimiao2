package cn.a10miaomiao.bilimiao.compose.components.pager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * 支持边缘滑动打开抽屉的 HorizontalPager
 *
 * 当触摸在左边缘区域（默认40dp）时，水平滑动会打开抽屉而不是切换页面。
 * 只有当触摸从非边缘区域开始时，才进行页面切换。
 *
 * @param pageCount 总页数
 * @param edgeThreshold 边缘检测阈值，默认40dp
 * @param onEdgeSwipeOpen 当检测到边缘滑动打开抽屉时的回调
 * @param modifier 修饰器
 * @param initialPage 初始页码
 * @param pagerState 分页状态
 * @param userScrollEnabled 是否允许页面滚动
 * @param content 分页内容
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawerAwareHorizontalPager(
    pageCount: Int,
    edgeThreshold: Dp = 40.dp,
    onEdgeSwipeOpen: () -> Unit,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
    pagerState: PagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageCount }),
    userScrollEnabled: Boolean = true,
    content: @Composable (page: Int) -> Unit,
) {
    DrawerAwareHorizontalPager(
        modifier = modifier,
        pagerState = pagerState,
        edgeThreshold = edgeThreshold,
        onEdgeSwipeOpen = onEdgeSwipeOpen,
        userScrollEnabled = userScrollEnabled,
        content = content,
    )
}

/**
 * 支持边缘滑动打开抽屉的 HorizontalPager
 *
 * @param pagerState 分页状态
 * @param edgeThreshold 边缘检测阈值，默认40dp
 * @param onEdgeSwipeOpen 当检测到边缘滑动打开抽屉时的回调
 * @param modifier 修饰器
 * @param userScrollEnabled 是否允许页面滚动
 * @param content 分页内容
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawerAwareHorizontalPager(
    pagerState: PagerState,
    edgeThreshold: Dp = 40.dp,
    onEdgeSwipeOpen: () -> Unit,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    content: @Composable (page: Int) -> Unit,
) {
    val density = LocalDensity.current
    val edgePx = with(density) { 100.dp.toPx() }

    // 是否允许 Pager 滑动
    var pagerScrollEnabled by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        userScrollEnabled = pagerScrollEnabled,
        modifier = modifier
    ) { page ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown()

                            val isEdge = down.position.x <= edgePx

                            if (isEdge) {
                                // 👉 让 Drawer 接管
                                pagerScrollEnabled = false

                                // 等待手势结束再恢复
                                waitForUpOrCancellation()
                                pagerScrollEnabled = true
                            }
                        }
                    }
                }
        ) {
            content(page)
        }
    }
}
