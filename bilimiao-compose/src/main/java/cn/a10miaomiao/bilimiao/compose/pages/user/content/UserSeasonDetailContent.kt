package cn.a10miaomiao.bilimiao.compose.pages.user.content

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import bilibili.app.view.v1.ViewGRPC
import cn.a10miaomiao.bilimiao.compose.comm.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.commponents.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.commponents.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.commponents.video.VideoItem
import com.a10miaomiao.bilimiao.comm.entity.media.MediasInfo
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

private class UserSeasonDetailViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val fragment: Fragment by instance()
    val userStore: UserStore by instance()

    var sid: String = ""
        set(value) {
            if (field != value) {
                field = value
                loadData(1)
            }
        }

    var keyword = ""

    val isRefreshing = MutableStateFlow(false)
    val list = FlowPaginationInfo<bilibili.app.view.v1.Episode>()


    private fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            list.loading.value = true
            val req = bilibili.app.view.v1.SeasonReq(
                seasonId = sid.toLong(),
            )
            val res = BiliGRPCHttp.request {
                ViewGRPC.season(req)
            }.awaitCall()
            list.data.value = res.season?.sections?.get(0)?.episodes ?: listOf()
            list.finished.value = true
            list.pageNum = pageNum
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
        isRefreshing.value = true
        list.finished.value = false
        list.fail.value = ""
        loadData(1)
    }

    fun loadMore() {
        if (!list.finished.value && !list.loading.value) {
            loadData(list.pageNum + 1)
        }
    }

    fun toVideoDetailPage(item: MediasInfo) {
        fragment.findNavController()
            .navigate(
                Uri.parse("bilimiao://video/" + item.id),
                defaultNavOptions,
            )
    }
}

@Composable
internal fun UserSeasonDetailContent(
    seasonId: String,
    seasonTitle: String,
) {
    val viewModel: UserSeasonDetailViewModel = diViewModel()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(seasonId) {
        viewModel.sid = seasonId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp + windowInsets.topDp.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(top = windowInsets.topDp.dp),
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(text = seasonTitle)
                }
                HorizontalDivider(
                    modifier = Modifier.align(Alignment.BottomStart)
                        .fillMaxWidth()
                )
            }
        }
        SwipeToRefresh(
            modifier = Modifier.weight(1f),
            refreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(400.dp),
            ) {
                items(list) {
                    VideoItem(
                        title = it.title,
//                        pic = it.cover,
//                        upperName = it.author.name,
//                        playNum = it.stat.view.toString(),
//                        damukuNum = it.stat.danmaku.toString(),
//                        duration = NumberUtil.converDuration(it.page.duration),
                        onClick = {
//                            viewModel.toVideoDetailPage(it)
                        }
                    )
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
}