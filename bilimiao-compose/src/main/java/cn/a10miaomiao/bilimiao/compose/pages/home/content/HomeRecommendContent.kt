package cn.a10miaomiao.bilimiao.compose.pages.home.content

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import bilibili.app.card.v1.Card
import bilibili.app.card.v1.SmallCoverV5
import bilibili.app.show.v1.EntranceShow
import bilibili.app.show.v1.PopularGRPC
import bilibili.app.show.v1.PopularResultReq
import cn.a10miaomiao.bilimiao.compose.common.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.navigation.findComposeNavController
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.components.video.MiniVideoItemBox
import cn.a10miaomiao.bilimiao.compose.components.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.home.HomeRecommendInfo
import com.a10miaomiao.bilimiao.comm.entity.home.RecommendCardInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance


@Stable
private class HomeRecommendContentViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val context: Context by instance()
    private val fragment: Fragment by instance()
    private val filterStore: FilterStore by instance()

    private val lastIdx
        get() = list.data.value.lastOrNull()?.idx ?: 0
    val list = FlowPaginationInfo<RecommendCardInfo>()
    val isRefreshing = MutableStateFlow(false)
    val listStyle = MutableStateFlow(0)



    init {
        viewModelScope.launch {
            SettingPreferences.run {
                context.dataStore.data.map {
                    it[HomeRecommendListStyle] ?: 0
                }
            }.collect {
                listStyle.value = it
            }
        }
        loadData(0)
    }

    private fun loadData(
        idx: Long = lastIdx
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val res = BiliApiService.homeApi.recommendList(
                idx = idx,
            ).awaitCall().gson<ResultInfo<HomeRecommendInfo>>()
            if (res.isSuccess) {
                val itemsList = res.data.items
                val filterList = itemsList.filter {
                    (it.goto?.isNotEmpty() ?: false)
                            && filterStore.filterWord(it.title)
                            && filterStore.filterUpper(it.args.up_id ?: "-1")
                            && filterStore.filterTag(it.param, it.card_goto)
                }
                if (idx == 0L) {
                    list.data.value = filterList
                } else {
                    list.data.value = list.data.value.toMutableList().also {
                        it.addAll(filterList)
                    }
                }
                list.finished.value = itemsList.isEmpty()
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

    fun toVideoDetail(item: RecommendCardInfo) {
        if (item.goto == "av" || item.goto == "vertical_av") {
            val nav = fragment.findNavController()
            nav.navigate(
                Uri.parse("bilimiao://video/" + item.param),
                defaultNavOptions,
            )
        } else if (item.goto == "bangumi") {
            val nav = fragment.findComposeNavController()
            nav.navigate(BangumiDetailPage(
                epId = item.param
            ))
        }
//        else if (!BiliNavigation.navigationTo(view, item.uri)){
//            BiliNavigation.navigationToWeb(requireActivity(), item.uri)
//        }
    }


}

@Composable
internal fun HomeRecommendContent() {
    val viewModel: HomeRecommendContentViewModel = diViewModel()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val listStyle by viewModel.listStyle.collectAsState()

    SwipeToRefresh(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
    ) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = if (listStyle == 0) GridCells.Adaptive(300.dp)
                else GridCells.Adaptive(180.dp),
            contentPadding = windowInsets.toPaddingValues(
                top = 0.dp,
            )
        ) {
            items(list, { it.idx }) {
                if (listStyle == 0) {
                    VideoItemBox(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        title = it.title,
                        pic = it.cover,
                        upperName = it.args.up_name,
                        playNum = it.cover_left_text_1,
                        damukuNum = it.cover_left_text_2,
                        duration = it.cover_right_text,
                        onClick = {
                            viewModel.toVideoDetail(it)
                        }
                    )
                } else {
                    MiniVideoItemBox(
                        modifier = Modifier.padding(5.dp),
                        title = it.title,
                        pic = it.cover,
                        upperName = it.args.up_name,
                        playNum = it.cover_left_text_1,
                        damukuNum = it.cover_left_text_2,
                        duration = it.cover_right_text,
                        onClick = {
                            viewModel.toVideoDetail(it)
                        }
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