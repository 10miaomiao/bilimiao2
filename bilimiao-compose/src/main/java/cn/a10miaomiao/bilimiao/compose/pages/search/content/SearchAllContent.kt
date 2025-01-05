package cn.a10miaomiao.bilimiao.compose.pages.search.content

import android.net.Uri
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.dynamic.v2.DynamicGRPC
import bilibili.app.dynamic.v2.DynamicItem
import bilibili.polymer.app.search.v1.Item.CardItem
import bilibili.polymer.app.search.v1.SearchGRPC
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.constant.PageTabIds
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.emitter.EmitterAction
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.localEmitter
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.dyanmic.DynamicItemCard
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.pages.search.components.MoreConditionsDialog
import cn.a10miaomiao.bilimiao.compose.pages.search.components.MoreConditionsDialogState
import cn.a10miaomiao.bilimiao.compose.pages.search.components.SearchItemCard
import com.a10miaomiao.bilimiao.comm.mypage.MenuActions
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.RegionStore
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

typealias SearchItem = bilibili.polymer.app.search.v1.Item

private class SearchAllContentViewModel(
    override val di: DI,
    val keyword: String,
) : ViewModel(), DIAware {

    private val pageNavigation: PageNavigation by instance()
    private val regionStore: RegionStore by instance()

    private var _next = ""
    val list = FlowPaginationInfo<SearchItem>()
    val isRefreshing = MutableStateFlow(false)

    val rankOrderList = listOf(
        0 to "默认排序",
        2 to "新发布",
        1 to "播放多",
        3 to "弹幕多",
    )
    val rankOrder = mutableStateOf(rankOrderList[0])

    val moreConditionsDialogState = MoreConditionsDialogState(
        regionStore,
        onConfirm = ::confirmConditions
    )
    val hasFilter = mutableStateOf(false)

    init {
        loadData("")
    }

    private fun loadData(
        next: String = _next
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val moreConditions = moreConditionsDialogState.data
            val order = rankOrder.value.first
            val timeType = moreConditions.timeType
                .let { if (it == 0) "" else "${it}d" }
            val tidList = moreConditions.regionList
                .joinToString(",")
            val durationList = moreConditions.durationList
                .joinToString(",")
            list.loading.value = true
            val req = bilibili.polymer.app.search.v1.SearchAllRequest(
                keyword = keyword,
                order = order,
                timeType = timeType,
                tidList = tidList,
                durationList = durationList,
                pagination = bilibili.pagination.Pagination(
                    pageSize = list.pageSize,
                    next = next
                )
            )
            val result = BiliGRPCHttp.request {
                SearchGRPC.searchAll(req)
            }.awaitCall()
            val itemList = if (next.isNotBlank()
                || hasFilter.value
                || order != 0
            ) {
                result.item.filter { it.cardItem is CardItem.Av }
            } else {
                result.item
            }
            _next = result.pagination?.next ?: ""
            list.finished.value = itemList.isEmpty() || _next.isBlank()
            if (next.isBlank()) {
                list.data.value = itemList
            } else {
                list.data.value = list.data.value
                    .toMutableList()
                    .apply { addAll(itemList) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = e.message ?: e.toString()
            list.loading.value = false
        } finally {
            list.loading.value = false
            isRefreshing.value = false
        }
    }


    fun tryAgainLoadData() {
        if (!list.loading.value && !list.finished.value) {
            loadData()
        }
    }

    fun loadMore() {
        if (!list.loading.value && !list.finished.value) {
            loadData(_next)
        }
    }

    fun refresh() {
        list.reset()
        isRefreshing.value = true
        loadData("")
    }

    fun confirmConditions() {
        val moreConditions = moreConditionsDialogState.data
        hasFilter.value = moreConditions.timeType != 0
                || moreConditions.regionList[0] != 0
                || moreConditions.durationList[0] != 0
        refresh()
    }

    fun menuItemClick(view: View, item: MenuItemPropInfo) {
        val key = item.key ?: return
        when (key) {
            in 10..19 -> {
                val order = key - 10
                rankOrder.value = rankOrderList.find {
                    it.first == order
                } ?: rankOrderList[0]
                refresh()
            }
            MenuKeys.filter -> {
                moreConditionsDialogState.open()
            }
        }
    }

    fun toDetailPage(item: SearchItem) {
        pageNavigation.navigateByUri(
            Uri.parse(item.uri)
        )
    }

}

@Composable
private fun SearchAllContentConfig(
    keyword: String,
    viewModel: SearchAllContentViewModel,
) {
    val rankOrder by viewModel.rankOrder
    val hasFilter by viewModel.hasFilter
    val pageConfigId = PageConfig(
        title = "搜索\n-\n$keyword",
        menu = rememberMyMenu(rankOrder, hasFilter) {
            myItem {
                key = MenuKeys.search
                action = MenuActions.search
                title = "继续搜索"
                iconFileName = "ic_search_gray"
            }
            myItem {
                key = MenuKeys.sort
                title = rankOrder.second
                iconFileName = "ic_baseline_filter_list_grey_24"
                childMenu = myMenu {
                    checkable = true
                    checkedKey = 10 + rankOrder.first
                    viewModel.rankOrderList.forEach {
                        myItem {
                            title = it.second
                            key = 10 + it.first
                        }
                    }
                }
            }
            myItem {
                key = MenuKeys.filter
                title = if (hasFilter) "已筛选" else "筛选"
                iconFileName = "ic_baseline_filter_list_alt_24"
            }
        },
        search = SearchConfigInfo(
            keyword = keyword
        )
    )
    PageListener(
        pageConfigId,
        onMenuItemClick = viewModel::menuItemClick
    )
}

@Composable
internal fun SearchAllContent(
    keyword: String,
    isActive: Boolean,
) {
    val viewModel = diViewModel(
        key = PageTabIds.SearchAll + keyword
    ) {
        SearchAllContentViewModel(it, keyword)
    }
    if (isActive) {
        SearchAllContentConfig(keyword, viewModel)
    }
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val listState = rememberLazyGridState()
    val emitter = localEmitter()
    LaunchedEffect(Unit) {
        emitter.collectAction<EmitterAction.DoubleClickTab> {
            if (it.tab == PageTabIds.SearchAll) {
                if (listState.firstVisibleItemIndex == 0) {
                    viewModel.refresh()
                } else {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }

    SwipeToRefresh(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
    ) {
        LazyVerticalGrid(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Adaptive(300.dp),
            contentPadding = windowInsets.toPaddingValues(
                top = 0.dp,
            )
        ) {
            items(list) {
                val cardItem = it.cardItem
                if (cardItem != null) {
                    SearchItemCard(
                        cardItem,
                        onClick = {
                            viewModel.toDetailPage(it)
                        }
                    )
                }
            }
            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                ListStateBox(
                    loading = listLoading,
                    finished = listFinished,
                    fail = listFail,
                    listData = list,
                ) {
                    viewModel.loadMore()
                }
            }
        }
    }

    MoreConditionsDialog(
        state = viewModel.moreConditionsDialogState
    )

}
