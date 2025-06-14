package cn.a10miaomiao.bilimiao.compose.pages.user.content

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
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
import bilibili.app.dynamic.v2.DynamicItem
import bilibili.app.interfaces.v1.SearchArchiveReq
import bilibili.app.interfaces.v1.SearchDynamicReq
import bilibili.app.interfaces.v1.SpaceGRPC
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.constant.PageTabIds
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.emitter.EmitterAction
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.localEmitter
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.dyanmic.DynamicItemCard
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance


private class UserSearchDynamicContentViewModel(
    override val di: DI,
    private val mid: Long,
    private val keyword: String,
) : ViewModel(), DIAware {

    private val pageNavigation: PageNavigation by instance()

    val isRefreshing = MutableStateFlow(false)
    val list = FlowPaginationInfo<DynamicItem>()

    init {
        loadData(1)
    }

    private fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            list.loading.value = true
            val req = SearchDynamicReq(
                keyword = keyword,
                mid = mid,
                pn = pageNum.toLong(),
                ps = list.pageSize.toLong(),
            )
            val res = BiliGRPCHttp.request {
                SpaceGRPC.searchDynamic(req)
            }.awaitCall()

            val archivesList = res.dynamics.map {
                it.dynamic
            }.filterNotNull()
            if (pageNum == 1) {
                list.data.value = archivesList
            } else {
                list.data.value = list.data.value
                    .toMutableList()
                    .apply { addAll(archivesList) }
            }
            list.finished.value = archivesList.size < list.pageSize
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = e.message ?: e.toString()
        } finally {
            list.loading.value = false
            isRefreshing.value = false
        }
    }

    fun tryAgainLoadData() = loadData()

    fun refresh() {
        isRefreshing.value = true
        list.reset()
        loadData(1)
    }

    fun loadMore() {
        if (!list.finished.value && !list.loading.value) {
            loadData(list.pageNum + 1)
        }
    }

    fun toDetailPage(
        item: DynamicItem
    ) {
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
fun UserSearchDynamicContent(
    mid: Long,
    keyword: String,
) {
    val viewModel = diViewModel(
        key = "${PageTabIds.UserSearchDynamic}:$mid:$keyword"
    ) {
        UserSearchDynamicContentViewModel(it, mid, keyword)
    }
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val listFlow = viewModel.list
    val list by listFlow.data.collectAsState()
    val listLoading by listFlow.loading.collectAsState()
    val listFinished by listFlow.finished.collectAsState()
    val listFail by listFlow.fail.collectAsState()

    val listState = rememberLazyListState()
    val emitter = localEmitter()
    LaunchedEffect(Unit) {
        emitter.collectAction<EmitterAction.DoubleClickTab> {
            if (it.tab == PageTabIds.UserSearchDynamic) {
                if (listState.firstVisibleItemIndex == 0) {
                    viewModel.refresh()
                } else {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
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
                isJumpToUser = false,
                onClick = {
                    viewModel.toDetailPage(it)
                },
            )
        }
        item {
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