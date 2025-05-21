package cn.a10miaomiao.bilimiao.compose.pages.mine.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.common.constant.PageTabIds
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.emitter.EmitterAction
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.localEmitter
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.bangumi.BangumiItemBox
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResponseResult
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo2
import com.a10miaomiao.bilimiao.comm.entity.bangumi.MyBangumiFollowListInfo
import com.a10miaomiao.bilimiao.comm.entity.bangumi.MyBangumiInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

class TypeBangumiContentViewModel(
    override val di: DI,
    private val type: String,
) : ViewModel(), DIAware {
    private val pageNavigation by instance<PageNavigation>()

    val typeName = if (type == "cinema") "追剧" else "追番"
    val statusList = listOf(
        0 to "全部$typeName",
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
            val res = BiliApiService.bangumiAPI
                .followList(
                    type = type,
                    status = status,
                    pageNum = pageNum,
                    pageSize = list.pageSize,
                )
                .awaitCall()
                .json<ResponseResult<MyBangumiFollowListInfo>>()
            if (res.isSuccess) {
                list.pageNum = pageNum
                val result = res.requireData()
                val followList = result.follow_list ?: listOf()
                list.finished.value = result.has_next != 1
                if (pageNum == 1) {
                    list.data.value = followList
                } else {
                    val listData = list.data.value.toMutableList()
                    listData.addAll(followList.filter { row ->
                        listData.indexOfFirst { it.season_id == row.season_id } == -1
                    })
                    list.data.value = listData
                }
            } else {
                list.fail.value = res.message
            }
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = e.message ?: e.toString()
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
        pageNavigation.navigate(
            BangumiDetailPage(
            id = item.season_id,
        )
        )
    }

    fun changeFollowStatus(
        item: MyBangumiInfo,
        status: Int,
    ) = viewModelScope.launch {
        try {
            val res = if (status == 0) {
                BiliApiService.bangumiAPI
                    .cancelFollow(
                        seasonId = item.season_id,
                    )
                    .awaitCall()
                    .json<MessageInfo>(isLog = true)
            } else {
                BiliApiService.bangumiAPI
                    .setFollowStatus(
                        seasonId = item.season_id,
                        status = status,
                    )
                    .awaitCall()
                    .json<MessageInfo>(isLog = true)
            }
            if (res.isSuccess) {
                list.data.value = list.data.value.filter {
                    it.season_id != item.season_id
                }
                if (status == 0) {
                    PopTip.show("已取消$typeName")
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
fun TypeBangumiContent(
    type: String,
    isActive: Boolean,
) {
    val viewModel = diViewModel(key = type) {
        TypeBangumiContentViewModel(it, type)
    }
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    if (isActive) {
        PageConfig(
            title = "我的${viewModel.typeName}"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(windowInsets.toPaddingValues(
                top = 0.dp,
                bottom = 0.dp
            ))
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
                listOf(0 to "取消${viewModel.typeName}")
            } else {
                listOf(
                    Pair(0,"取消$${viewModel.typeName}"),
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

        val listState = rememberLazyGridState()
        val emitter = localEmitter()
        LaunchedEffect(Unit) {
            emitter.collectAction<EmitterAction.DoubleClickTab> {
                if (it.tab == PageTabIds.MyBangumi[type]) {
                    if (listState.firstVisibleItemIndex == 0) {
                        viewModel.refresh()
                    } else {
                        listState.animateScrollToItem(0)
                    }
                }
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
                state = listState,
                columns = GridCells.Adaptive(300.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(list, { it.season_id }) {
                    BangumiItemBox(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        title = it.title,
                        cover = it.cover,
                        statusText = it.new_ep.index_show,
                        desc = it.progress?.index_show,
                        moreMenu = moreMenu,
                        coverBadge1 = {
                            val badge = it.badge_info
                            if (badge != null && badge.text.isNotBlank()) {
                                Text(
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .background(
                                            color = Color(badge.bg_color.toColorInt()),
                                            shape = RoundedCornerShape(5.dp)
                                        )
                                        .padding(vertical = 2.dp, horizontal = 4.dp),
                                    text = badge.text,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        },
                        coverBadge2 = {
                            if (it.season_type == 4) {
                                Text(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(
                                                topEnd = 10.dp,
                                            )
                                        )
                                        .padding(vertical = 2.dp, horizontal = 4.dp),
                                    text = "国产动漫",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        },
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