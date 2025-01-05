package cn.a10miaomiao.bilimiao.compose.pages.search.content

import android.net.Uri
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.polymer.app.search.v1.SearchByTypeRequest
import bilibili.polymer.app.search.v1.SearchGRPC
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
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.pages.search.components.SearchItemCard
import com.a10miaomiao.bilimiao.comm.mypage.MenuActions
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance


private class SearchByTypeContentViewModel(
    override val di: DI,
    val type: Int, // 用户：2，直播：4，图文：6，番剧：7，影视：8，
    val keyword: String,
) : ViewModel(), DIAware {

    private val pageNavigation: PageNavigation by instance()

    private var _next = ""
    val list = FlowPaginationInfo<SearchItem>()
    val isRefreshing = MutableStateFlow(false)

    val userSortList = listOf(
        SearchByTypeRequest.UserSort.DEFAULT to "默认排序",
        SearchByTypeRequest.UserSort.FANS_DESCEND to "粉丝数由高到低",
        SearchByTypeRequest.UserSort.FANS_ASCEND to "粉丝数由低到高",
        SearchByTypeRequest.UserSort.LEVEL_DESCEND to "Lv等级由高到低",
        SearchByTypeRequest.UserSort.LEVEL_ASCEND to "Lv等级由低到高",
    )
    val userSort = mutableStateOf(userSortList[0])

    val userTypeList = listOf(
        SearchByTypeRequest.UserType.ALL to "全部",
        SearchByTypeRequest.UserType.UP to "UP主",
        SearchByTypeRequest.UserType.NORMAL_USER to "认证用户",
        SearchByTypeRequest.UserType.AUTHENTICATED_USER to "普通用户",
    )
    val userType = mutableStateOf(userTypeList[0])

    init {
        loadData("")
    }

    private fun loadData(
        next: String = _next
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val req = SearchByTypeRequest(
                keyword = keyword,
                type = type,
                pagination = bilibili.pagination.Pagination(
                    pageSize = list.pageSize,
                    next = next
                )
            ).let {
                if (type == 2) it.copy(
                    userSort = userSort.value.first,
                    userType = userType.value.first,
                ) else it
            }
            val result = BiliGRPCHttp.request {
                SearchGRPC.searchByType(req)
            }.awaitCall()
            val itemList = result.items
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

    fun menuItemClick(view: View, item: MenuItemPropInfo) {
        val key = item.key ?: return
        when (key) {
            in 10..19 -> {
                val sort = key - 10
                userSort.value = userSortList.find {
                    it.first.value == sort
                } ?: userSortList[0]
                refresh()
            }
            in 20..29 -> {
                val type = key - 20
                userType.value = userTypeList.find {
                    it.first.value == type
                } ?: userTypeList[0]
                refresh()
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
private fun SearchByTypeContentConfig(
    type: Int, // 用户：2，直播：4，图文：6，番剧：7，影视：8，
    keyword: String,
    viewModel: SearchByTypeContentViewModel,
) {
    val userSort by viewModel.userSort
    val userType by viewModel.userType
    val pageConfigId = PageConfig(
        title = "搜索\n-\n$keyword",
        menu = rememberMyMenu(type, userSort, userType) {
            myItem {
                key = MenuKeys.search
                action = MenuActions.search
                title = "继续搜索"
                iconFileName = "ic_search_gray"
            }
            if (type == 2) {
                myItem {
                    key = MenuKeys.sort
                    title = userSort.second
                    iconFileName = "ic_baseline_filter_list_grey_24"
                    childMenu = myMenu {
                        checkable = true
                        checkedKey = 10 + userSort.first.value
                        viewModel.userSortList.forEach {
                            myItem {
                                title = it.second
                                key = 10 + it.first.value
                            }
                        }
                    }
                }
                myItem {
                    key = MenuKeys.filter
                    title = userType.second
                    iconFileName = "ic_baseline_filter_list_alt_24"
                    childMenu = myMenu {
                        checkable = true
                        checkedKey = 20 + userType.first.value
                        viewModel.userTypeList.forEach {
                            myItem {
                                title = it.second
                                key = 20 + it.first.value
                            }
                        }
                    }
                }
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
internal fun SearchByTypeContent(
    type: Int, // 用户：2，直播：4，图文：6，番剧：7，影视：8，
    keyword: String,
    isActive: Boolean
) {
    val viewModel = diViewModel(
        key = PageTabIds.SearchByType[type] + keyword
    ) {
        SearchByTypeContentViewModel(it, type, keyword)
    }
    if (isActive) {
        SearchByTypeContentConfig(type, keyword, viewModel)
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
            if (it.tab == PageTabIds.SearchByType[type]) {
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

}

