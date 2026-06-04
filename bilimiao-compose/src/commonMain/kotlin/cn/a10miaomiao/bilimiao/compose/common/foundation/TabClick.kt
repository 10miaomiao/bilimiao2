package cn.a10miaomiao.bilimiao.compose.common.foundation

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun combinedTabDoubleClick(
    pagerState: PagerState,
    onDoubleClick: (Int) -> Unit,
): (Int) -> Unit {
    val scope = rememberCoroutineScope()
    val lastClickTime = remember { arrayOf(0L) }
    fun tabClick(index: Int) {
        if (pagerState.currentPage == index) {
            val nowTime = System.currentTimeMillis()
            if (nowTime - lastClickTime[0] < 2000L) {
                lastClickTime[0] = 0L
                onDoubleClick(index)
            } else {
                lastClickTime[0] = System.currentTimeMillis()
            }
        } else {
            scope.launch {
                pagerState.animateScrollToPage(index)
            }
        }
    }
    return ::tabClick
}