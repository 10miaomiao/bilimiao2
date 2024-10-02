package cn.a10miaomiao.bilimiao.compose.pages.user.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.dynamic.v2.DynamicGRPC
import cn.a10miaomiao.bilimiao.compose.comm.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.components.dyanmic.DynamicItemCard
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

private class UserDynamicListContentViewModel(
    override val di: DI,
    private val vmid: String,
) : ViewModel(), DIAware {

    val fragment: Fragment by instance()

    val isRefreshing = MutableStateFlow(false)
    val list = FlowPaginationInfo<bilibili.app.dynamic.v2.DynamicItem>()
    var listHistoryOffset = ""

    init {
        loadData(1)
    }

    private fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO) {
        miaoLogger() debug "loadData, pageNum:${pageNum}"
        try {
            list.loading.value = true
            val req = bilibili.app.dynamic.v2.DynSpaceReq(
                hostUid = vmid.toLong(),
                historyOffset = listHistoryOffset,
                page = pageNum.toLong(),
            )
            val res = BiliGRPCHttp.request {
                DynamicGRPC.dynSpace(req)
            }.awaitCall()
            listHistoryOffset = res.historyOffset
            list.finished.value = !res.hasMore
            list.pageNum = pageNum
            if (pageNum == 1) {
                list.data.value = res.list.toMutableList()
            } else {
                list.data.value = mutableListOf<bilibili.app.dynamic.v2.DynamicItem>()
                    .apply {
                        addAll(list.data.value)
                        addAll(res.list)
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = "无法连接到御坂网络"
        } finally {
            list.loading.value = false
            isRefreshing.value = false
        }
    }

    fun tryAgainLoadData() = loadData()

    fun refresh() {
        listHistoryOffset = ""
        isRefreshing.value = true
        list.finished.value = false
        list.fail.value = ""
        loadData(1)
    }

    fun loadMore() {
        miaoLogger() debug "loadMore, ${list.finished.value}, ${list.loading.value} "
        if (!list.finished.value && !list.loading.value) {
            miaoLogger() debug "loadMore, ${list.pageNum + 1} "
            loadData(list.pageNum + 1)
        }
    }

    override fun onCleared() {
        miaoLogger() debug "onCleared"
        super.onCleared()
    }
}

@Composable
fun UserDynamicListContent(
    vmid: String,
) {
    val viewModel = diViewModel(key = "dynamic-$vmid") {
        UserDynamicListContentViewModel(it, vmid)
    }
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val listFlow = viewModel.list
    val list by listFlow.data.collectAsState()
    val listLoading by listFlow.loading.collectAsState()
    val listFinished by listFlow.finished.collectAsState()
    val listFail by listFlow.fail.collectAsState()

    LazyColumn(
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
                    PopTip.show("施工中，ヽ(￣ω￣(￣ω￣〃)ゝ")
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