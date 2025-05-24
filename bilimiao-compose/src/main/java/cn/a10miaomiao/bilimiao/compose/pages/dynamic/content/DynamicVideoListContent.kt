package cn.a10miaomiao.bilimiao.compose.pages.dynamic.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import bilibili.app.dynamic.v2.DynamicGRPC
import cn.a10miaomiao.bilimiao.compose.BilimiaoPageRoute
import cn.a10miaomiao.bilimiao.compose.common.constant.PageTabIds
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.emitter.EmitterAction
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.localEmitter
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.dyanmic.DynamicModuleAuthorBox
import cn.a10miaomiao.bilimiao.compose.components.dyanmic.DynamicModuleStatBox
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.components.video.MiniVideoItemBox
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

class DynamicVideoListContentViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val pageNavigation: PageNavigation by instance()
    private val filterStore: FilterStore by instance()

    private var _offset = ""
    private var _baseline = ""
    val list = FlowPaginationInfo<DynamicVideoInfo>()
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
            val req = bilibili.app.dynamic.v2.DynVideoReq(
                refreshType = type,
                localTime = 8,
                offset = offset,
                updateBaseline = _baseline,
            )
            val result = BiliGRPCHttp.request {
                DynamicGRPC.dynVideo(req)
            }.awaitCall()
            val dynamicList = result.dynamicList
            if (dynamicList != null) {
                _offset = dynamicList.historyOffset
                _baseline = dynamicList.updateBaseline
                val itemsList = dynamicList.list.filter { item ->
                    item.cardType != bilibili.app.dynamic.v2.DynamicType.DYN_NONE
                            && item.cardType != bilibili.app.dynamic.v2.DynamicType.AD
                }.map { item ->
                    val modules = item.modules
                    val userModule = modules.first { it.moduleAuthor != null }.moduleAuthor!!
                    val descModule = modules.find { it.moduleDesc != null }?.moduleDesc
                    val dynamicModule = modules.first { it.moduleDynamic != null }.moduleDynamic!!
                    val statModule = modules.first { it.moduleStat != null }.moduleStat!!
                    val author = userModule.author!!
                    DynamicVideoInfo(
                        mid = author.mid.toString(),
                        name = author.name,
                        face = author.face,
                        labelText = userModule.ptimeLabelText,
                        locationText = userModule.ptimeLocationText,
                        dynamicType = dynamicModule.type.value,
                        share = statModule.repost,
                        like = statModule.like,
                        reply = statModule.reply,
                        isLike = statModule.likeInfo?.isLike == true,
                        dynamicContent = getDynamicContent(dynamicModule),
                    )
                }
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
        loadData("")
    }

    private fun getDynamicContent(
        dynamicModule: bilibili.app.dynamic.v2.ModuleDynamic
    ): DynamicVideoContentInfo {
        return dynamicModule.dynArchive?.let {
            DynamicVideoContentInfo(
                id = it.avid.toString(),
                title = it.title,
                pic = it.cover,
                remark = it.coverLeftText2 + "    " + it.coverLeftText3,
                duration = it.coverLeftText1,
            )
        } ?: dynamicModule.dynPgc?.let {
            DynamicVideoContentInfo(
                id = it.seasonId.toString(),
                title = it.title,
                pic = it.cover,
                remark = it.coverLeftText2 + "    " + it.coverLeftText3,
            )
        } ?: DynamicVideoContentInfo("")
    }

    fun toVideoDetail(item: DynamicVideoInfo) {
        when(item.dynamicType) {
            bilibili.app.dynamic.v2.ModuleDynamicType.MDL_DYN_ARCHIVE.value -> {
                pageNavigation.navigateToVideoInfo(item.dynamicContent.id)
            }
            bilibili.app.dynamic.v2.ModuleDynamicType.MDL_DYN_PGC.value -> {
                pageNavigation.navigate(BangumiDetailPage(
                    id = item.dynamicContent.id
                ))
            }
            else -> {
                PopTip.show("未知跳转类型")
            }
        }
    }

    fun toUserSpace(mid: String) {
        pageNavigation.navigate(UserSpacePage(
            id = mid,
        ))
    }
}

@Composable
fun DynamicVideoListContent() {
    val viewModel = diViewModel<DynamicVideoListContentViewModel>(
//        key = "dynamic-video-list"
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val listState = rememberLazyGridState()
    val emitter = localEmitter()
    LaunchedEffect(Unit) {
        emitter.collectAction<EmitterAction.DoubleClickTab> {
            if (it.tab == PageTabIds.DynamicVideo) {
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
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            columns = GridCells.Adaptive(300.dp),
            contentPadding = windowInsets.toPaddingValues(
                top = 0.dp,
            )
        ) {
            items(list) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .clickable { viewModel.toVideoDetail(it) }
                ) {
                    DynamicModuleAuthorBox(
                        name = it.name,
                        face = it.face,
                        labelText = it.labelText,
                        locationText = it.locationText,
                        onClick = {
                            viewModel.toUserSpace(it.mid)
                        }
                    )
                    VideoItemBox(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                        ),
                        title = it.dynamicContent.title,
                        pic = it.dynamicContent.pic,
                        remark = it.dynamicContent.remark,
                        duration = it.dynamicContent.duration,
                    )
                    DynamicModuleStatBox(
                        share = it.share,
                        like = it.like,
                        reply = it.reply,
                        isLike = it.isLike,
                    )
                }
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