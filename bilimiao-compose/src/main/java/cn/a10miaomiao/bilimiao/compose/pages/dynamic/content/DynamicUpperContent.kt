package cn.a10miaomiao.bilimiao.compose.pages.dynamic.content

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bilibili.app.dynamic.v2.UpListItem
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import org.kodein.di.compose.rememberInstance
import androidx.compose.foundation.shape.RoundedCornerShape
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import com.a10miaomiao.bilimiao.store.WindowStore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.dynamic.v2.DynamicGRPC
import bilibili.app.dynamic.v2.DynamicItem
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.constant.PageTabIds
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.emitter.EmitterAction
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localEmitter
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.dyanmic.DynamicItemCard
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class DynamicUpperContentViewModel(
    override val di: DI,
    val dynamicViewModel: DynamicViewModel,
    val upper: UpListItem,
) : ViewModel(), DIAware {

    private val pageNavigation: PageNavigation by instance()
    private val filterStore: FilterStore by instance()

    private var _offset = ""
    private var _baseline = ""
    val list = FlowPaginationInfo<DynamicItem>()
    val isRefreshing = MutableStateFlow(false)

    init {
        loadData("")
    }


    private fun loadData(
        offset: String = _offset
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val req = bilibili.app.dynamic.v2.DynAllPersonalReq(
                hostUid = upper.uid,
                localTime = 8,
                offset = offset,
            )
            val result = BiliGRPCHttp.request {
                DynamicGRPC.dynAllPersonal(req)
            }.awaitCall()
            val dynamicList = result.list
            _offset = result.offset
            if (offset.isBlank()) {
                list.data.value = dynamicList
            } else {
                list.data.value = list.data.value
                    .toMutableList()
                    .apply { addAll(dynamicList) }
            }
            list.finished.value = !result.hasMore
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

    fun toDetailPage(item: DynamicItem) {
        val extend = item.extend ?: return
        val toUrl = extend.cardUrl
        try {
            pageNavigation.navigateByUri(Uri.parse(toUrl))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun DynamicUpperContent(
    dynamicViewModel: DynamicViewModel,
    upper: UpListItem,
) {
    val viewModel = diViewModel(
        key = "dynamic-upper-" + upper.uid,
    ) {
        DynamicUpperContentViewModel(it, dynamicViewModel, upper)
    }
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val listState = rememberLazyListState()
    val emitter = localEmitter()
    LaunchedEffect(Unit) {
        emitter.collectAction<EmitterAction.DoubleClickTab> {
            if (it.tab == PageTabIds.DynamicAll) {
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
        LazyColumn(
            state = listState,
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
                        viewModel.toDetailPage(it)
                    },
                )
            }
            item() {
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