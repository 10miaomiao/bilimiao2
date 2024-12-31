package cn.a10miaomiao.bilimiao.compose.pages.dynamic.content

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.dynamic.v2.DynamicGRPC
import bilibili.app.dynamic.v2.DynamicItem
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.emitter.EmitterAction
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.localEmitter
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.dyanmic.DynamicItemCard
import cn.a10miaomiao.bilimiao.compose.components.dyanmic.DynamicModuleAuthorBox
import cn.a10miaomiao.bilimiao.compose.components.dyanmic.DynamicModuleStatBox
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.components.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicVideoContentInfo
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicVideoInfo
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance


class DynamicAllListContenttViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val pageNavigation: PageNavigation by instance()
    private val filterStore: FilterStore by instance()

    private var _offset = ""
    private var _baseline = ""
    val list = FlowPaginationInfo<DynamicItem>()
    val isRefreshing = MutableStateFlow(false)

    init {
        loadData("")
    }


    private fun loadData(
        offset: String = _offset
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val type = if (offset.isBlank()) {
                bilibili.app.dynamic.v2.Refresh.NEW
            } else {
                bilibili.app.dynamic.v2.Refresh.HISTORY
            }
            val req = bilibili.app.dynamic.v2.DynAllReq(
                refreshType = type,
                localTime = 8,
                offset = offset,
                updateBaseline = _baseline,
            )
            val result = BiliGRPCHttp.request {
                DynamicGRPC.dynAll(req)
            }.awaitCall()
            val dynamicList = result.dynamicList
            if (dynamicList != null) {
                _offset = dynamicList.historyOffset
                _baseline = dynamicList.updateBaseline
                val itemsList = dynamicList.list
                if (offset.isBlank()) {
                    list.data.value = itemsList
                } else {
                    list.data.value = list.data.value
                        .toMutableList()
                        .apply { addAll(itemsList) }
                }
            } else {
                list.data.value = listOf()
                list.finished.value = true
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
            loadData(_offset)
        }
    }

    fun refresh() {
        list.reset()
        isRefreshing.value = true
        loadData()
    }

    fun toDetailPage(item: DynamicItem) {
        val extend = item.extend ?: return
        val toUrl = extend.cardUrl
        try {
            pageNavigation.navigateByUri(Uri.parse(toUrl))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun DynamicAllListContent() {
    val viewModel = diViewModel<DynamicAllListContenttViewModel>(
//        key = "dynamic-all-list"
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val listState = rememberLazyListState()
    val emitter = localEmitter()
    LaunchedEffect(Unit) {
        emitter.collectAction<EmitterAction.DoubleClickTab> {
            if (it.tab == "dynamic.all") {
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
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = windowInsets.addPaddingValues(
                addTop = -windowInsets.topDp.dp + 10.dp,
                addBottom = 10.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(list) {
                DynamicItemCard(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth(),
                    item = it,
                    onClick = {
                        viewModel.toDetailPage(it)
                    },
                )
            }
            item() {
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