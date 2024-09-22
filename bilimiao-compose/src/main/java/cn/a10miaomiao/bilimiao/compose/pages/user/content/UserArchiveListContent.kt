package cn.a10miaomiao.bilimiao.compose.pages.user.content

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.comm.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.commponents.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.commponents.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.user.MyFollowViewModel
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpaceViewModel
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveCursorInfo
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediasInfo
import com.a10miaomiao.bilimiao.comm.entity.user.SpaceInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

private class UserArchiveListContentViewModel(
    override val di: DI,
    private val vmid: String,
) : ViewModel(), DIAware {

    val fragment: Fragment by instance()
//    var regionList = listOf<CheckPopupMenu.MenuItemInfo<Int>>(
//        CheckPopupMenu.MenuItemInfo("全部(0)", 0),
//    )
//    var region = regionList[0]
//
//    val rankOrderList = listOf<CheckPopupMenu.MenuItemInfo<String>>(
//        CheckPopupMenu.MenuItemInfo("最新发布", "pubdate"),
//        CheckPopupMenu.MenuItemInfo("最多播放", "click"),
////        CheckPopupMenu.MenuItemInfo("最多收藏", "stow"),
//    )
    var rankOrder = MutableStateFlow("pubdate")

    val isRefreshing = MutableStateFlow(false)
//    var total = 0
    val list = FlowPaginationInfo<ArchiveInfo>()
    private var lastAid: String = ""

//
    init {
        loadData("")
    }

    private fun loadData(
        aid: String = lastAid
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            list.loading.value = true
            val res = BiliApiService.userApi
                .upperVideoList(
                    vmid = vmid,
//                    tid = region.value,
                    order = rankOrder.value,
                    aid = aid,
                    pageSize = list.pageSize,
                )
                .awaitCall()
                .gson<ResultInfo<ArchiveCursorInfo>>()
            if (res.code == 0) {
                val items: List<ArchiveInfo> = res.data.item ?: emptyList()
                if (aid.isBlank()) {
                    list.data.value = items.toMutableList()
                } else {
                    list.data.value = mutableListOf<ArchiveInfo>().apply {
                        addAll(list.data.value)
                        addAll(items)
                    }
                }
                lastAid = items.lastOrNull()?.param ?: ""
//                if (region.value == 0) {
//                    total = res.data.count
//                }
                list.finished.value = !res.data.has_next
            } else {
                PopTip.show(res.message)
                throw Exception(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = e.message ?: e.toString()
        } finally {
            list.loading.value = false
            isRefreshing.value = false
        }
    }

    private fun tryAgainLoadData() {
        loadData()
    }

    fun loadMore() {
        if (!list.finished.value && !list.loading.value) {
            loadData(lastAid)
        }
    }

    fun refreshList() {
        isRefreshing.value = true
        list.finished.value = false
        list.fail.value = ""
    }

    fun toVideoDetail(item: ArchiveInfo) {
        fragment.findNavController()
            .navigate(
                Uri.parse("bilimiao://video/" + item.param),
                defaultNavOptions,
            )
    }
}

@Composable
fun UserArchiveListContent(
    vmid: String,
) {
    val viewModel = diViewModel(key = "archive-${vmid}") {
        UserArchiveListContentViewModel(it, vmid)
    }
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val listFlow = viewModel.list
    val list by listFlow.data.collectAsState()
    val listLoading by listFlow.loading.collectAsState()
    val listFinished by listFlow.finished.collectAsState()
    val listFail by listFlow.fail.collectAsState()

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Adaptive(300.dp),
        contentPadding = windowInsets.toPaddingValues(
            addTop = -windowInsets.topDp.dp,
        )
    ) {
        items(list) {
            VideoItemBox(
                Modifier.padding(10.dp),
                title = it.title,
                pic = it.cover,
                playNum = it.play,
                damukuNum = it.danmaku,
                remark = NumberUtil.converCTime(it.ctime),
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