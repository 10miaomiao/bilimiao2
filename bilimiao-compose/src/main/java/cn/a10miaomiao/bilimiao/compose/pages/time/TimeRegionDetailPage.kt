package cn.a10miaomiao.bilimiao.compose.pages.time

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.BilimiaoPageRoute
import cn.a10miaomiao.bilimiao.compose.base.BottomSheetState
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.flow.stateMap
import cn.a10miaomiao.bilimiao.compose.common.foundation.pagerTabIndicatorOffset
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.pages.time.content.TimeRegionDetailListContent
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import com.a10miaomiao.bilimiao.comm.store.TimeSettingStore
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
data class TimeRegionDetailPage(
    private val tid: Int,
    private val name: String,
    private val childIds: List<Int>,
    private val childNames: List<String>,
    private val initialIndex: Int,
): ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: TimeRegionDetailPageViewModel = diViewModel {
            TimeRegionDetailPageViewModel(
                it, tid, name, childIds, childNames, initialIndex
            )
        }
//        LaunchedEffect(initialIndex) {
//            viewModel.setPageIndex(initialIndex)
//        }
        TimeRegionDetailPageContent(viewModel)
    }

}

private class TimeRegionDetailPageViewModel(
    override val di: DI,
    val tid: Int,
    val name: String,
    val childIds: List<Int>,
    val childNames: List<String>,
    val initialIndex: Int,
) : ViewModel(), DIAware {

    private val timeSettingStore: TimeSettingStore by instance()
    private val bottomSheetState by instance<BottomSheetState>()

    val timeText = timeSettingStore.stateFlow.stateMap {
        "${it.timeFrom.getValue("-")}\n至\n${it.timeTo.getValue("-")}"
    }

    val rankOrder = timeSettingStore.stateFlow.stateMap {
        it.getRankOrderKey()
    }

    val pagerState = PagerState(
        currentPage = initialIndex
    ) { childIds.size }

    suspend fun setPageIndex(index: Int) {
        pagerState.scrollToPage(index)
    }

    fun getRankOrderText(): String {
        return timeSettingStore.getRankOrderText()
    }

    fun menuItemClick(view: View, item: MenuItemPropInfo) {
        when (item.key) {
            MenuKeys.time -> {
                bottomSheetState.open(TimeSettingPage())
            }
            in 0..4 -> {
                timeSettingStore.setRankOrder(item.key!!)
            }
        }
    }


}


@Composable
private fun TimeRegionDetailPageContent(
    viewModel: TimeRegionDetailPageViewModel
) {
    val timeText by viewModel.timeText.collectAsState()
    val rankOrder by viewModel.rankOrder.collectAsState()
    val pageConfigId = PageConfig(
        title = "时光姬\n-\n${viewModel.name}",
        menu = rememberMyMenu(timeText, rankOrder) {
            myItem {
                key = MenuKeys.filter
                iconFileName = "ic_baseline_filter_list_grey_24"
                title = viewModel.getRankOrderText()
                childMenu = myMenu {
                    checkable = true
                    checkedKey = rankOrder
                    myItem {
                        key = 0
                        title = "播放数"
                    }
                    myItem {
                        key = 1
                        title = "评论数"
                    }
                    myItem {
                        key = 2
                        title = "收藏数"
                    }
                    myItem {
                        key = 3
                        title = "硬币数"
                    }
                    myItem {
                        key = 4
                        title = "弹幕数"
                    }
                }
            }
            myItem {
                key = MenuKeys.time
                title = "当前时间线"
                subTitle = timeText
            }
        },
    )
    PageListener(
        configId = pageConfigId,
        onMenuItemClick = viewModel::menuItemClick,
    )

    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val pagerState = viewModel.pagerState
    val currentPage = pagerState.currentPage
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            ScrollableTabRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(
                        top = windowInsets.topDp.dp,
                        start = windowInsets.leftDp.dp,
                        end = windowInsets.rightDp.dp,
                    ),
                edgePadding = 0.dp,
                selectedTabIndex = currentPage,
                indicator = { positions ->
                    if (currentPage > -1 && currentPage < positions.size) {
                        TabRowDefaults.PrimaryIndicator(
                            Modifier.pagerTabIndicatorOffset(pagerState, positions),
                        )
                    }
                },
                divider = {}
            ) {
                viewModel.childIds.forEachIndexed { index, id ->
                    Tab(
                        text = {
                            Text(
                                text = viewModel.childNames[index],
                                color = if (index == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                }
                            )
                        },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                    )
                }
            }
            HorizontalDivider(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
            )
        }

        val saveableStateHolder = rememberSaveableStateHolder()
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = pagerState,
        ) { index ->
            val id = viewModel.childIds[index]
            saveableStateHolder.SaveableStateProvider(id) {
                TimeRegionDetailListContent(
                    rid = id,
                )
            }
        }

    }

}