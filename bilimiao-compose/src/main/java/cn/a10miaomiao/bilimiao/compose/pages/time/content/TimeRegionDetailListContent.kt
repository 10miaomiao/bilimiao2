package cn.a10miaomiao.bilimiao.compose.pages.time.content

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.common.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.components.video.VideoItemBox
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.region.RegionVideoInfo
import com.a10miaomiao.bilimiao.comm.entity.region.RegionVideosRankInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import com.a10miaomiao.bilimiao.comm.store.TimeSettingStore
import com.a10miaomiao.bilimiao.comm.store.model.DateModel
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

private class TimeRegionDetailListContentViewModel(
    override val di: DI,
    private val rid: Int,
) : ViewModel(), DIAware {

    private val pageNavigation by instance<PageNavigation>()

    private val timeSettingStore: TimeSettingStore by instance()
    private val filterStore: FilterStore by instance()

    var timeFrom = DateModel()
    var timeTo = DateModel()
    var rankOrder = "click"  //排行依据

    val isRefreshing = MutableStateFlow(false)
    val list = FlowPaginationInfo<RegionVideoInfo>()

    private var tryAgainTimes = 0

    init {
        val timeSettingState = timeSettingStore.state
        timeFrom = timeSettingState.timeFrom
        timeTo = timeSettingState.timeTo
        rankOrder = timeSettingState.rankOrder
        loadData()
    }

    private fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            list.loading.value = true
            val res = BiliApiService.regionAPI
                .regionVideoList(
                    rid = rid,
                    rankOrder = rankOrder,
                    pageNum = pageNum,
                    pageSize = list.pageSize,
                    timeFrom = timeFrom.getValue(),
                    timeTo = timeTo.getValue(),
                )
                .awaitCall()
                .json<ResultInfo<RegionVideosRankInfo>>()
            if (res.code == 0) {
                val result = res.data.result
                if (result == null) {
                    // result为null重新请求
                    tryAgainTimes++
                    // 只重试5次
                    if (tryAgainTimes > 5) {
                        PopTip.show("获取不到列表数据")
                        throw Exception(res.message)
                    }
                    delay(2000L)
                    tryAgainLoadData(pageNum)
                    return@launch
                }
                tryAgainTimes = 0
                if (result.size < list.pageSize) {
                    list.finished.value = true
                }
                val listData = result.filter {
                    filterStore.filterWord(it.title)
                            && filterStore.filterUpper(it.mid.toLong())
                }
                if (pageNum == 1) {
                    list.data.value = listData
                } else {
                    list.data.value = list.data.value
                        .toMutableList()
                        .also { it.addAll(listData) }
                }
                list.pageNum = pageNum
                if (list.data.value.size < 10 && listData.size != result.size) {
                    // 列表数据少于10个 且 屏蔽前数量与屏蔽后数量不等
                    tryAgainLoadData(pageNum + 1)
                }
            } else {
                PopTip.show(res.message)
                throw Exception(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = e.message ?: e.toString()
            tryAgainTimes = 0
        } finally {
            list.loading.value = false
            isRefreshing.value = false
        }
    }

    fun tryAgainLoadData(pageNum: Int = list.pageNum) {
        loadData(pageNum)
    }

    fun loadMore () {
        if (!list.finished.value && !list.loading.value) {
            loadData(
                pageNum = list.pageNum + 1
            )
        }
    }

    fun refresh() {
        list.reset()
        isRefreshing.value = true
        loadData()
    }

    fun toVideoDetail(
        item: RegionVideoInfo
    ) {
        pageNavigation.navigateToVideoInfo(item.id)
    }
}

@Composable
fun TimeRegionDetailListContent(
    rid: Int,
) {
    val viewModel = diViewModel(key = rid.toString()) {
        TimeRegionDetailListContentViewModel(it, rid)
    }
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val timeSettingStore: TimeSettingStore by rememberInstance()
    val timeState by timeSettingStore.stateFlow.collectAsState()
    LaunchedEffect(timeState) {
        if (
            viewModel.timeFrom != timeState.timeFrom
            || viewModel.timeTo != timeState.timeTo
            || viewModel.rankOrder != timeState.rankOrder
        ) {
            viewModel.timeFrom = timeState.timeFrom
            viewModel.timeTo = timeState.timeTo
            viewModel.rankOrder = timeState.rankOrder
            viewModel.refresh()
        }
    }

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    SwipeToRefresh(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
    ) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Adaptive(300.dp),
            contentPadding = windowInsets.toPaddingValues(
                top = 0.dp,
            )
        ) {
            items(list, { it.id }) {
                VideoItemBox(
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                    title = it.title,
                    pic =it.pic,
                    upperName = it.author,
                    playNum = it.play,
                    damukuNum = it.video_review,
                    duration = NumberUtil.converDuration(it.duration),
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