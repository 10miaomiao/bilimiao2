package cn.a10miaomiao.bilimiao.compose.pages.dynamic.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bilibili.app.dynamic.v2.UpListItem
import cn.a10miaomiao.bilimiao.compose.common.constant.PageTabIds
import cn.a10miaomiao.bilimiao.compose.common.emitter.EmitterAction
import cn.a10miaomiao.bilimiao.compose.common.foundation.combinedTabDoubleClick
import cn.a10miaomiao.bilimiao.compose.common.foundation.pagerTabIndicatorOffset
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.localEmitter
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Composable
fun DynamicPageScaffold(
    allContent: @Composable () -> Unit,
    videoContent: @Composable () -> Unit,
    upperList: @Composable (maxWidth: Dp) -> Unit,
    upperContent: @Composable (upItem: UpListItem) -> Unit,
    pagerState: PagerState,
    selectedUpper: UpListItem? = null,
) {
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())
    val scope = rememberCoroutineScope()

    BoxWithConstraints {
        val isMiniUpList = maxWidth < 600.dp
        val upListMaxWidth = if (isMiniUpList) {
            72.dp
        } else if (maxWidth < 1000.dp) {
            180.dp
        } else {
            200.dp
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (!isMiniUpList) {
                upperList(upListMaxWidth)
            }
            HorizontalPager(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                state = pagerState,
            ) { index ->
                if (index == 0) {
                    DynamicAllAndVideoWrap(
                        allContent = allContent,
                        videoContent = videoContent,
                        toUpper = {
                            scope.launch {
                                if (pagerState.pageCount > 1) {
                                    pagerState.animateScrollToPage(1)
                                } else {
                                    PopTip.show("请先登录喵")
                                }
                            }
                        },
                        isMiniUpList = isMiniUpList,
                    )
                } else if (selectedUpper != null) {
                    DynamicUpperWrap(
                        upperList = upperList,
                        isMiniUpList = isMiniUpList,
                        upperContent = upperContent,
                        selectedUpper = selectedUpper,
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicAllAndVideoWrap(
    allContent: @Composable () -> Unit,
    videoContent: @Composable () -> Unit,
    toUpper: () -> Unit,
    isMiniUpList: Boolean = false,
) {
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val scope = rememberCoroutineScope()
    val tabs = listOf(
        PageTabIds.DynamicAll to "动态",
        PageTabIds.DynamicVideo to "视频",
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val emitter = localEmitter()
    val combinedTabClick = combinedTabDoubleClick(
        pagerState = pagerState,
        onDoubleClick = {
            scope.launch {
                emitter.emit(
                    EmitterAction.DoubleClickTab(
                        tab = tabs[it].first
                    )
                )
            }
        }
    )


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isMiniUpList) {
            TabRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(windowInsets.toPaddingValues(bottom = 0.dp)),
                selectedTabIndex = pagerState.currentPage,
                indicator = { positions ->
                    TabRowDefaults.PrimaryIndicator(
                        Modifier.pagerTabIndicatorOffset(pagerState, positions),
                    )
                },
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        text = {
                            Text(
                                text = tab.second,
                                color = if (index == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                }
                            )
                        },
                        selected = pagerState.currentPage == index,
                        onClick = { combinedTabClick(index) },
                    )
                }
                // 右侧“最常访问”按钮
                TextButton(
                    onClick = toUpper,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "最常访问",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(
                    horizontal = 8.dp
                )
            ) {
                items(tabs.size, { it }) { index ->
                    val tab = tabs[index]
                    FilterChip(
                        selected = index == pagerState.currentPage,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        label = {
                            Text(text = tab.second)
                        }
                    )
                }
            }
        }
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = pagerState,
        ) { index ->
            if (index == 0) {
                allContent()
            } else {
                videoContent()
            }
        }
    }
}

@Composable
fun DynamicUpperWrap(
    upperList: @Composable (maxWidth: Dp) -> Unit,
    upperContent: @Composable (upItem: UpListItem) -> Unit,
    isMiniUpList: Boolean = false,
    selectedUpper: UpListItem,
) {
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(windowInsets.toPaddingValues())
    ) {
        if (isMiniUpList) {
            upperList(72.dp)
        }
        // 左侧为所选UP主的动态列表占位
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            upperContent(selectedUpper)
        }
    }
}