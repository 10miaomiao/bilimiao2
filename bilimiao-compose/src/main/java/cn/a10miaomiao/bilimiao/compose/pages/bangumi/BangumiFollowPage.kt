package cn.a10miaomiao.bilimiao.compose.pages.bangumi

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.base.navigate
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.comm.navigation.findComposeNavController
import cn.a10miaomiao.bilimiao.compose.commponents.bangumi.BangumiItemBox
import cn.a10miaomiao.bilimiao.compose.commponents.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.commponents.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.pages.user.FollowingItemInfo
import cn.a10miaomiao.bilimiao.compose.pages.user.InterrelationInfo
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo2
import com.a10miaomiao.bilimiao.comm.entity.bangumi.MyBangumiFollowListInfo
import com.a10miaomiao.bilimiao.comm.entity.bangumi.MyBangumiInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance


class BangumiFollowPage : ComposePage() {
    override val route: String
        get() = "bangumi/follow"

    @Composable
    override fun AnimatedContentScope.Content(navEntry: NavBackStackEntry) {
        val viewModel: BangumiFollowPageViewModel = diViewModel()
        BangumiFollowPageContent(viewModel)
    }
}

private class BangumiFollowPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()

    val statusList = listOf(
        0 to "全部番剧",
        1 to "想看",
        2 to "在看",
        3 to "看过",
    )
    val currentStatus = MutableStateFlow(0)
    val isRefreshing = MutableStateFlow(false)
    val list = FlowPaginationInfo<MyBangumiInfo>()

    init {
        viewModelScope.launch {
            currentStatus.collect {
                loadData(1)
            }
        }
    }

    fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val status = currentStatus.value
            val res = BiliApiService.userBangumiAPI
                .followList(
                    status = status,
                    pageNum = pageNum,
                    pageSize = list.pageSize,
                )
                .awaitCall()
                .gson<ResultInfo2<MyBangumiFollowListInfo>>()
            if (res.isSuccess) {
                list.pageNum = pageNum
                val result = res.result
                list.finished.value = result.has_next != 1
                if (pageNum == 1) {
                    list.data.value = result.follow_list
                } else {
                    val listData = list.data.value.toMutableList()
                    listData.addAll(result.follow_list)
                    list.data.value = listData
                }
            } else {
                list.fail.value = res.message
            }
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

    fun updateStatus(
        status: Int
    ) {
        currentStatus.value = status
    }

    fun toDetailPage(item: MyBangumiInfo) {
        val nav = fragment.findComposeNavController()
        nav.navigate(BangumiDetailPage()) {
            id set item.season_id
        }
    }

    fun changeFollowStatus(
        item: MyBangumiInfo,
        status: Int,
    ) = viewModelScope.launch {
        try {
            val res = if (status == 0) {
                BiliApiService.userBangumiAPI
                    .followDel(
                        seasonId = item.season_id,
                    )
                    .awaitCall()
                    .gson<MessageInfo>()
            } else {
                BiliApiService.userBangumiAPI
                    .followUpdate(
                        seasonId = item.season_id,
                        status = status,
                    )
                    .awaitCall()
                    .gson<MessageInfo>()
            }
            if (res.isSuccess) {
                list.data.value = list.data.value.filter {
                    it.season_id != item.season_id
                }
                if (status == 0) {
                    PopTip.show("已取消追番")
                } else {
                    PopTip.show("操作成功")
                }
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show("操作失败：" + (e.message ?: e.toString()))
        }

    }
}


@Composable
private fun BangumiFollowPageContent(
    viewModel: BangumiFollowPageViewModel
) {
    PageConfig(
        title = "我的追番"
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = windowInsets.topDp.dp,
                start = windowInsets.leftDp.dp,
                end = windowInsets.rightDp.dp,
            )
    ) {
        val currentStatus = viewModel.currentStatus.collectAsState().value
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                horizontal = 8.dp
            )
        ) {
            items(viewModel.statusList, { it.first }) {
                FilterChip(
                    selected = it.first == currentStatus,
                    onClick = {
                        viewModel.updateStatus(it.first)
                    },
                    label = {
                        Text(text = it.second)
                    }
                )
            }
        }

        val list by viewModel.list.data.collectAsState()
        val listLoading by viewModel.list.loading.collectAsState()
        val listFinished by viewModel.list.finished.collectAsState()
        val listFail by viewModel.list.fail.collectAsState()
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        val moreMenu = remember(currentStatus) {
            if (currentStatus == 0) {
                listOf(0 to "取消追番")
            } else {
                listOf(
                    Pair(0,"取消追番"),
                    Pair(1, "标记为『想看』").takeIf {
                        currentStatus != 1
                    },
                    Pair(2, "标记为『在看』").takeIf {
                        currentStatus != 2
                    },
                    Pair(3, "标记为『看过』").takeIf {
                        currentStatus != 3
                    },
                )
            }
        }

        SwipeToRefresh(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            refreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(400.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(list, { it.season_id }) {
                    BangumiItemBox(
                        title = it.title,
                        cover = it.cover,
                        statusText = it.new_ep.index_show,
                        desc = it.progress?.index_show,
                        isChinaMade = it.season_type == 4,
                        badgeText = it.badge_info?.text,
                        badgeColor = it.badge_info?.bg_color?.let {
                            Color(android.graphics.Color.parseColor(it))
                        } ?: MaterialTheme.colorScheme.primary,
                        moreMenu = moreMenu,
                        onMenuItemClick = { menu ->
                            viewModel.changeFollowStatus(it, menu.first)
                        },
                        onClick = {
                            viewModel.toDetailPage(it)
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