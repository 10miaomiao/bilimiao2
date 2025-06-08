package cn.a10miaomiao.bilimiao.compose.pages.rank.content

import android.app.Activity
import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.show.v1.RankAllResultReq
import bilibili.app.show.v1.RankGRPC
import bilibili.app.show.v1.RankRegionResultReq
import cn.a10miaomiao.bilimiao.compose.common.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.components.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.mine.MyFollowViewModel
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

private class RankListContentViewModel(
    override val di: DI,
    val regionId: Int,
) : ViewModel(), DIAware {

    private val pageNavigation by instance<PageNavigation>()

    val count = MutableStateFlow(1)
    val isRefreshing = MutableStateFlow(false)
    val list = FlowPaginationInfo<bilibili.app.show.v1.Item>()

    init {
        loadData()
    }


    fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val result = BiliGRPCHttp.request {
                if (regionId == 0) {
                    val req = RankAllResultReq(
                        order = "all",
                        pn = pageNum,
                        ps = list.pageSize
                    )
                    RankGRPC.rankAll(req)
                } else {
                    val req = RankRegionResultReq(
                        rid = regionId,
                        pn = pageNum,
                        ps = list.pageSize
                    )
                    RankGRPC.rankRegion(req)
                }
            }.awaitCall()
//            var totalCount = 0 // 屏蔽前数量
//            if (result.size < list.pageSize) {
//                ui.setState { list.finished = true }
//            }
//            totalCount = result.size
//            result = result.filter {
//                filterStore.filterWord(it.title)
//                        && filterStore.filterUpper(it.mid.toLong())
//            }
            if (pageNum == 1) {
                list.data.value = result.items
            } else {
                list.data.value = list.data.value
                    .toMutableList()
                    .also { it.addAll(result.items) }
            }
            list.pageNum = pageNum
//            if (list.data.size < 10 && totalCount != result.size) {
//                _loadData(pageNum + 1)
//            }
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = "无法连接到御坂网络"
        } finally {
            list.loading.value = false
            isRefreshing.value = false
        }
    }

    fun loadMore() {
        if (!list.finished.value && !list.loading.value) {
            loadData(list.pageNum + 1)
        }
    }

    fun refresh(
        refreshing: Boolean = true
    ) {
        isRefreshing.value = refreshing
        list.finished.value = false
        list.fail.value = ""
        loadData(1)
    }

    fun toVideoDetail(item: bilibili.app.show.v1.Item) {
        pageNavigation.navigateToVideoInfo(item.param)
    }
}

@Composable
internal fun RankListContent(
    regionId: Int,
) {
    val viewModel = diViewModel(key = regionId.toString()) {
        RankListContentViewModel(it, regionId)
    }
    val myFollowViewModel: MyFollowViewModel by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val userStore: UserStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLogin = userStore.isLogin()

    SwipeToRefresh(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(330.dp),
            modifier = Modifier.padding(
                start = windowInsets.leftDp.dp,
                end = windowInsets.rightDp.dp,
            )
        ) {
            items(list.size, { list[it].param }) {
                val item = list[it]
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(
                            end = 10.dp,
                            top = 5.dp,
                            bottom = 5.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.width(30.dp),
                        text = (it + 1).toString(),
                        textAlign = TextAlign.Center,
                        color = if (it > 2) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    VideoItemBox(
                        modifier = Modifier.weight(1f),
                        title = item.title,
                        pic = item.cover,
                        upperName = item.name,
                        playNum = item.play.toString(),
                        damukuNum = item.danmaku.toString(),
                        onClick = {
                            viewModel.toVideoDetail(item)
                        }
                    )
                }
            }

            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                ListStateBox(
                    modifier = Modifier.padding(
                        bottom = windowInsets.bottomDp.dp
                    ),
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