package cn.a10miaomiao.bilimiao.compose.pages.mine

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.interfaces.v1.CursorItem
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.components.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import com.a10miaomiao.bilimiao.comm.entity.ListAndCountInfo
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.history.ToViewInfo
import com.a10miaomiao.bilimiao.comm.entity.history.ToViewItemInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
class WatchLaterPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: WatchLaterPageViewModel = diViewModel()
        WatchLaterPageContent(viewModel)
    }
}

private class WatchLaterPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val pageNavigation by instance<PageNavigation>()

    val isRefreshing = MutableStateFlow(false)
    var list = FlowPaginationInfo<ToViewItemInfo>()

    init {
        loadData(1)
    }

    private fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val res = BiliApiService.userApi
                .videoHistoryToview(
                    pageNum = pageNum,
                    pageSize = list.pageSize,
                )
                .awaitCall()
                .json<ResponseData<ToViewInfo>>()
            if (res.code == 0) {
                val listData = res.requireData().list
                if (pageNum == 1) {
                    list.data.value = listData.toMutableList()
                } else {
                    list.data.value = list.data.value
                        .toMutableList()
                        .apply { addAll(listData) }
                }
                list.pageNum = pageNum
                list.finished.value = listData.size < list.pageSize
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = e.message ?: e.toString()
        } finally {
            list.loading.value = false
            isRefreshing.value = false
        }
    }

//    fun deleteVideoHistoryToview(position: Int) = viewModelScope.launch(Dispatchers.IO) {
//        val item = list.data[position]
//        try {
//            val res = BiliApiService.userApi
//                .videoHistoryToviewDel(item.aid)
//                .awaitCall()
//                .gson<MessageInfo>()
//            if (res.code == 0) {
//                withContext(Dispatchers.Main) {
//                    PopTip.show("已从稍后再看移出")
//                }
//                ui.setState {
//                    list.data.removeAt(position)
//                }
//            } else {
//                withContext(Dispatchers.Main) {
//                    PopTip.show(res.message)
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            withContext(Dispatchers.Main) {
//                PopTip.show("失败:$e")
//            }
//        }
//    }

    private fun tryAgainLoadData() {
        loadData()
    }

    fun loadMore() {
        if (!list.finished.value && !list.loading.value) {
            loadData(list.pageNum + 1)
        }
    }

    fun refreshList() {
        isRefreshing.value = true
        list.reset()
        loadData(1)
    }

    fun toVideoDetail(item: ToViewItemInfo) {
        pageNavigation.navigateToVideoInfo(item.aid.toString())
    }
}


@Composable
private fun WatchLaterPageContent(
    viewModel: WatchLaterPageViewModel
) {
    PageConfig(
        title = "稍后再看"
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val listFlow = viewModel.list
    val list by listFlow.data.collectAsState()
    val listLoading by listFlow.loading.collectAsState()
    val listFinished by listFlow.finished.collectAsState()
    val listFail by listFlow.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    SwipeToRefresh(
        refreshing = isRefreshing,
        onRefresh = viewModel::refreshList,
    ) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Adaptive(300.dp),
            contentPadding = windowInsets.toPaddingValues()
        ) {
            items(list) {
                VideoItemBox(
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                    title = it.title,
                    pic = it.pic,
                    upperName = it.owner.name,
                    duration = NumberUtil.converDuration(it.duration),
                    playNum = it.left_text,
                    damukuNum = it.right_text,
                    onClick = {
                        viewModel.toVideoDetail(it)
                    }
                )
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