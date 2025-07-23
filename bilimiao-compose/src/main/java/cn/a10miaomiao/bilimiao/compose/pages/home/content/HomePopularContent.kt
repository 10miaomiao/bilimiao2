package cn.a10miaomiao.bilimiao.compose.pages.home.content

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.card.v1.Card
import bilibili.app.card.v1.SmallCoverV5
import bilibili.app.show.v1.EntranceShow
import bilibili.app.show.v1.PopularGRPC
import bilibili.app.show.v1.PopularResultReq
import cn.a10miaomiao.bilimiao.compose.common.constant.PageTabIds
import cn.a10miaomiao.bilimiao.compose.common.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.emitter.EmitterAction
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.localEmitter
import cn.a10miaomiao.bilimiao.compose.common.navigation.BilibiliNavigation
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.components.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.message.components.MessageItemBox
import cn.a10miaomiao.bilimiao.compose.pages.web.WebPage
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.message.AtMessageInfo
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance


@Stable
private class HomePopularContentViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val context: Context by instance()
    private val pageNavigation: PageNavigation by instance()
    private val filterStore: FilterStore by instance()

    private val lastIdx
        get() = list.data.value.lastOrNull()?.base?.idx ?: 0
    val list = FlowPaginationInfo<SmallCoverV5>()
    val topEntranceList = MutableStateFlow(listOf<EntranceShow>())
    val isRefreshing = MutableStateFlow(false)

    init {
        loadData(0)
    }

    private fun loadData(
        idx: Long = lastIdx
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val carryToken = SettingPreferences.mapData(context) {
                it[HomePopularCarryToken] ?: true
            }
            val req = PopularResultReq(
                idx = idx,
            )
            val result = BiliGRPCHttp.request {
                PopularGRPC.index(req)
            }.also {
                it.needToken = carryToken
            }.awaitCall()
            val itemsList = result.items
            val filterList = itemsList.mapNotNull {
                (it.item as? Card.Item.SmallCoverV5)?.value
            }.filter {
                val base = it.base
//                val upper = it?.up.id
                (base != null // && upper != null
                        && base.cardGoto == "av"
                        && filterStore.filterWord(base.title)
                        && filterStore.filterUpperName(it.rightDesc1))
            }
            val topItems = result.config?.topItems
            if (topItems != null) {
                topEntranceList.value = topItems
            }
            val newList = if (idx == 0L) mutableListOf()
                else list.data.value.toMutableList()
            if (filterStore.filterTagListIsEmpty()) {
                newList.addAll(filterList)
                list.data.value = newList
            } else {
                filterList.forEach {
                    if (filterStore.filterTag(it.base!!.param)) {
                        newList.add(it)
                        list.data.value = newList.toList()
                    }
                }
            }
            list.finished.value = itemsList.isEmpty()
            list.loading.value = false
            isRefreshing.value = false
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = e.message ?: e.toString()
            list.loading.value = false
            isRefreshing.value = false
        }
    }

    fun tryAgainLoadData() {
        loadData()
    }

    fun loadMore() {
        if (!list.finished.value && !list.loading.value) {
            loadData(lastIdx)
        }
    }

    fun refresh() {
        isRefreshing.value = true
        list.finished.value = false
        list.fail.value = ""
        loadData(0)
    }

    fun toVideoDetail(item: SmallCoverV5) {
        val base = item.base ?: return
        pageNavigation.navigateToVideoInfo(base.param)
    }

    fun toPageByUrl(url: String) {
        if (!BilibiliNavigation.navigationTo(pageNavigation, url)) {
            BilibiliNavigation.navigationToWeb(pageNavigation, url)
        }
    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun EntranceListBox(
    viewModel: HomePopularContentViewModel
) {
    val topEntranceList by viewModel.topEntranceList.collectAsState()
    LazyRow {
        items(topEntranceList, { it.uri }) {
            Column(
                modifier = Modifier.width(80.dp)
                    .clickable {
                        viewModel.toPageByUrl(it.uri)
                    }
                    .padding(top = 10.dp, bottom = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GlideImage(
                    model = UrlUtil.autoHttps(it.icon),
                    contentDescription = "",
                    modifier = Modifier
                        .padding(bottom = 5.dp)
                        .size(40.dp, 40.dp)
                )
                Text(
                    text = it.title,
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
internal fun HomePopularContent() {
    val viewModel: HomePopularContentViewModel = diViewModel()
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
            if (it.tab == PageTabIds.HomePopular) {
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
            state = listState,
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Adaptive(300.dp),
            contentPadding = windowInsets.toPaddingValues(
                top = 0.dp,
            )
        ) {
            item {
                EntranceListBox(viewModel)
            }
            items(list, { it.base!!.idx }) {
                VideoItemBox(
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                    title = it.base?.title,
                    pic =it.base?.cover,
                    upperName = it.rightDesc1,
                    remark = it.rightDesc2,
                    duration = it.coverRightText1,
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