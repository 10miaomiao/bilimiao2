package cn.a10miaomiao.bilimiao.compose.pages.user

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.R
import cn.a10miaomiao.bilimiao.compose.assets.BilimiaoIcons
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.Common
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Menufold
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Menuunfold
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.components.layout.AutoTwoPaneLayout
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoCard
import cn.a10miaomiao.bilimiao.compose.pages.user.components.TitleBar
import cn.a10miaomiao.bilimiao.compose.pages.user.content.UserMedialistDetailContent
import com.a10miaomiao.bilimiao.comm.entity.ItemAndCountInfo
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.archive.SeriesInfo
import com.a10miaomiao.bilimiao.comm.entity.archive.SeriesListInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListV2Info
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.store.WindowStore.Insets
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
data class UserMedialistPage(
    private val mid: String,
    private val bizId : String = "",
    private val bizType: String = "",
    private val bizTitle: String = "",
) : ComposePage() {

    @Composable
    override fun Content() {
        val defaultMedia = if (bizId.isNotBlank() && bizType.isNotBlank()) {
            UserMedialistPageViewModel.OpenedMediaInfo(
                id = bizId,
                type = bizType,
                title = bizTitle
            )
        } else null
        val viewModel: UserMedialistPageViewModel = diViewModel(key = mid) {
            UserMedialistPageViewModel(it, mid, defaultMedia)
        }
        UserMedialistPageContent(viewModel)
    }
}

private class UserMedialistPageViewModel(
    override val di: DI,
    val mid: String,
    val defaultMedia: OpenedMediaInfo?,
) : ViewModel(), DIAware {

    val isRefreshing = MutableStateFlow(false)
    val list = FlowPaginationInfo<SeriesInfo>()

    val _openedMedia = mutableStateOf<OpenedMediaInfo?>(null)
    val openedMedia get() = _openedMedia.value

    init {
        if (defaultMedia != null) {
            _openedMedia.value = defaultMedia
        }
    }

    fun initData() {
        if (!list.loading.value && list.data.value.isEmpty()) {
            loadData(1)
        }
    }

    private fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            list.loading.value = true
            val res = BiliApiService.userApi.upperSeriesList(
                mid = mid,
                pageNum = pageNum,
                pageSize = list.pageSize
            ).awaitCall().json<ResponseData<SeriesListInfo>>()
            if (res.isSuccess) {
                val result = res.requireData()
                if (pageNum == 1) {
                    list.data.value = result.items.toMutableList()
                } else {
                    list.data.value = listOf(
                        *list.data.value.toTypedArray(),
                        *result.items.toTypedArray()
                    )
                }
                list.finished.value = list.data.value.size >= result.page.total
                list.pageNum = pageNum
            } else {
                PopTip.show(res.message)
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

    fun tryAgainLoadData() = loadData()

    fun loadMore () {
        if (!list.finished.value && !list.loading.value) {
            loadData(list.pageNum + 1)
        }
    }

    fun refreshList() {
        isRefreshing.value = true
        list.finished.value = false
        list.fail.value = ""
        loadData(1)
    }

    fun openMediaDetail(item: SeriesInfo) {
        _openedMedia.value = OpenedMediaInfo(
            type = item.type,
            id = item.param,
            title = item.title
        )
    }

    fun closeMediaDetail() {
        _openedMedia.value = defaultMedia
    }

    data class OpenedMediaInfo(
        val type: String,
        val id: String,
        val title: String,
    ) {
        override fun equals(other: Any?): Boolean {
            if (other is OpenedMediaInfo) {
                return type == other.type && id == other.id
            }
            return super.equals(other)
        }
    }

}


@Composable
private fun UserMedialistPageContent(
    viewModel: UserMedialistPageViewModel,
) {
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    var hideFirstPane by remember {
        mutableStateOf(false)
    }

    PageConfig(
        title = "合集列表"
    )

    BackHandler(
        enabled = viewModel.openedMedia != null
                && viewModel.openedMedia != viewModel.defaultMedia,
        onBack = viewModel::closeMediaDetail
    )
    BackHandler(
        enabled = hideFirstPane,
        onBack = {
            hideFirstPane = false
        }
    )

    AutoTwoPaneLayout(
        modifier = Modifier.padding(
            start = windowInsets.leftDp.dp,
            end = windowInsets.rightDp.dp
        ),
        first = {
            UserMedialistListContent(
                showTowPane = it.showTowPane,
                viewModel = viewModel,
                windowInsets = windowInsets,
            )
        },
        second = {
            val media = viewModel.openedMedia
            if (media != null) {
                UserMedialistDetailContent(
                    bizType = media.type,
                    bizId = media.id,
                    bizTitle = media.title,
                    showTowPane = it.showTowPane,
                    hideFirstPane = hideFirstPane,
                    onChangeHideFirstPane = {
                        hideFirstPane = it
                    },
                )
            } else if (it.showTowPane) {
                val listFlow = viewModel.list
                val list by listFlow.data.collectAsState()
                val media = list.firstOrNull()
                if (media != null) {
                    UserMedialistDetailContent(
                        bizType = media.type,
                        bizId = media.param,
                        bizTitle = media.title,
                        showTowPane = true,
                        hideFirstPane = hideFirstPane,
                        onChangeHideFirstPane = {
                            hideFirstPane = it
                        },
                    )
                }
            }
        },
        twoPaneMinWidth = 500.dp,
        openedSecond = viewModel.openedMedia != null,
        firstPaneMaxWidth = 400.dp,
        hideFirstPane = hideFirstPane,
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun UserMedialistListContent(
    showTowPane: Boolean,
    viewModel: UserMedialistPageViewModel,
    windowInsets: Insets,
) {
    val isRefreshing = viewModel.isRefreshing.collectAsState().value
    val listFlow = viewModel.list
    val list by listFlow.data.collectAsState()
    val listLoading by listFlow.loading.collectAsState()
    val listFinished by listFlow.finished.collectAsState()
    val listFail by listFlow.fail.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.initData()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TitleBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp + windowInsets.topDp.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(top = windowInsets.topDp.dp),
            icon = {
                Spacer(modifier = Modifier.width(16.dp))
            },
            title = {
                Text(
                    text = "合集和系列",
                    style = MaterialTheme.typography.titleMedium,
                )
            },
        )

        SwipeToRefresh(
            modifier = Modifier.weight(1f),
            refreshing = isRefreshing,
            onRefresh = { viewModel.refreshList() },
        ) {
            LazyColumn {
                val selectedMedia = viewModel.openedMedia
                items(list.size, { list[it].param }) {
                    val item = list[it]
                    val isSelected = if (showTowPane) {
                        if (selectedMedia == null) it == 0
                        else selectedMedia.id == item.param && selectedMedia.type == item.type
                    } else { false }
                    MiaoCard(
                        modifier = Modifier.padding(5.dp),
                        onClick = {
                            viewModel.openMediaDetail(item)
                        },
                        enabled = !isSelected,
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable(
                                    onClick = {
                                        viewModel.openMediaDetail(item)
                                    },
                                    enabled = !isSelected,
                                )
                                .padding(10.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            GlideImage(
                                model = UrlUtil.autoHttps(item.cover) + "@672w_378h_1c_",
                                contentScale = ContentScale.Crop,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(width = 120.dp, height = 80.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                loading = placeholder(R.drawable.bili_default_placeholder_img_tv),
                                failure = placeholder(R.drawable.bili_fail_placeholder_img_tv),
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp)
                                    .padding(horizontal = 10.dp),
                            ) {
                                Text(
                                    text = item.title,
                                    maxLines = 2,
                                    modifier = Modifier.weight(1f),
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Text(
                                    text = "${item.count}个视频",
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.outline,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                item {
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