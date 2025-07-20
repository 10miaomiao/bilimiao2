package cn.a10miaomiao.bilimiao.compose.pages.user.content

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.assets.BilimiaoIcons
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.Common
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Menufold
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Menuunfold
import cn.a10miaomiao.bilimiao.compose.common.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.components.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.playlist.PlayListPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouriteDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.user.components.FavouriteEditForm
import cn.a10miaomiao.bilimiao.compose.pages.user.components.FavouriteEditFormState
import cn.a10miaomiao.bilimiao.compose.pages.user.components.TitleBar
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaDetailInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediasInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuActions
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

private class UserFavouriteDetailViewModel(
    override val di: DI,
    private val mediaId: String,
    private val mediaTitle: String,
    private val keyword: String = "",
) : ViewModel(), DIAware {

    private val pageNavigation: PageNavigation by instance()
    val userStore: UserStore by instance()
    private val playerDelegate: BasePlayerDelegate by instance()
    private val playerStore by instance<PlayerStore>()
    private val playListStore by instance<PlayListStore>()

    var mediaInfo = MutableStateFlow<MediaListInfo?>(null)
    val isRefreshing = MutableStateFlow(false)
    val list = FlowPaginationInfo<MediasInfo>()
    val isAutoPlay = MutableStateFlow(false)

    init {
        loadData(1)
    }

    private fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val res = BiliApiService.userApi.mediaDetail(
                media_id = mediaId,
                keyword = keyword,
                pageNum = pageNum,
                pageSize = list.pageSize
            ).awaitCall().json<ResponseData<MediaDetailInfo>>()
            if (res.code == 0) {
                val result = res.requireData()
                val mediaList = result.medias ?: listOf()
                mediaInfo.value = result.info
                if (pageNum == 1) {
                    list.data.value = mediaList
                } else {
                    list.data.value = mutableListOf<MediasInfo>().apply {
                        addAll(list.data.value)
                        addAll(mediaList)
                    }
                }
                list.finished.value = !result.has_more || mediaList.isEmpty()
                list.pageNum = pageNum
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

    fun tryAgainLoadData() = loadData()

    fun refresh() {
        mediaInfo.value = null
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

    fun changeAutoPlay(value: Boolean) {
        isAutoPlay.value = value
    }

    fun openVideo(item: MediasInfo) {
        val ugcInfo = item.ugc
        val ogvInfo = item.ogv
        if (isAutoPlay.value) {
            if (ugcInfo != null) {
                addPlayList()
                if (playerStore.state.aid != item.id) {
                    playerDelegate.openPlayer(
                        VideoPlayerSource(
                            mainTitle = item.title,
                            title = item.title,
                            coverUrl = item.cover,
                            aid = item.id,
                            id = ugcInfo.first_cid,
                            ownerId = item.upper.mid,
                            ownerName = item.upper.name,
                        )
                    )
                }
                return
            } else {
                PopTip.show("自动连播仅支持普通视频")
            }
        }
        if (ugcInfo != null) {
            pageNavigation.navigateToVideoInfo(item.id)
        } else if (ogvInfo != null) {
            pageNavigation.navigateToVideoInfo(ogvInfo.season_id)
        }
    }

    fun addPlayList() {
        val media = mediaInfo.value
        if (media == null) {
            PopTip.show("数据加载中，请稍后再试")
            return
        }
        playListStore.setFavoriteList(media.id, media.title)
    }

    fun toPlayListPage() {
        pageNavigation.navigate(PlayListPage())
    }

    suspend fun editFolder(
        title: String,
        cover: String,
        intro: String,
        privacy: Int, // 0:公开,1:不公开
    ) {
        val res = BiliApiService.userApi
            .favEditFolder(
                mediaId = mediaId,
                title = title,
                cover = cover,
                intro = intro,
                privacy = privacy,
            )
            .awaitCall()
            .json<MessageInfo>()
        if (!res.isSuccess) {
            throw Exception(res.message)
        }
    }

    suspend fun deleteFolder() {
        val res = BiliApiService.userApi
            .favDeleteFolder(
                mediaIds = mediaId,
            )
            .awaitCall()
            .json<MessageInfo>(isLog = true)
        if (!res.isSuccess) {
            throw Exception(res.message)
        }
    }

    fun favFolder() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = BiliApiService.userApi
                .favFavFolder(
                    mediaId = mediaId,
                )
                .awaitCall()
                .json<MessageInfo>()
            if (res.isSuccess) {
                PopTip.show("订阅成功")
                refresh()
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show(e.message ?: e.toString())
        }
    }

    fun unfavFolder() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = BiliApiService.userApi
                .favUnfavFolder(
                    mediaId = mediaId,
                )
                .awaitCall()
                .json<MessageInfo>()
            if (res.isSuccess) {
                PopTip.show("已取消订阅")
                refresh()
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show(e.message ?: e.toString())
        }
    }

    fun searchSelfPage(text: String) {
        pageNavigation.navigate(UserFavouriteDetailPage(
            id = mediaId,
            title = mediaTitle,
            keyword = text,
        ))
    }


    fun isSelfFav(): Boolean {
        return mediaInfo.value?.let {
            userStore.isSelf(it.mid.toString())
        } ?: true
    }

}

@Composable
internal fun UserFavouriteDetailContent(
    mediaId: String,
    mediaTitle: String,
    keyword: String = "",
    showTowPane: Boolean,
    hideFirstPane: Boolean,
    onChangeHideFirstPane: (hidden: Boolean) -> Unit,
    onClose: () -> Unit,
    onRefresh: () -> Unit,
) {
    val viewModel = diViewModel(key = mediaId + keyword) {
        UserFavouriteDetailViewModel(it, mediaId, mediaTitle, keyword)
    }

    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val detailInfo by viewModel.mediaInfo.collectAsState()
    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isAutoPlay by viewModel.isAutoPlay.collectAsState()

    val pageConfigId = PageConfig(
        title = if (keyword.isBlank()) "收藏详情" else "搜索\n-\n${keyword}",
        menu = remember(detailInfo) {
            myMenu {
                val selfFav = viewModel.isSelfFav()
                myItem {
                    key = MenuKeys.more
                    iconFileName = "ic_more_vert_grey_24dp"
                    title = "更多"
                    childMenu = myMenu {
                        myItem {
                            key = MenuKeys.playList
                            title = "设置为播放列表"
                        }
                        if (detailInfo != null && selfFav) {
                            myItem {
                                key = MenuKeys.edit
                                title = "编辑收藏夹"
                            }
                            if (detailInfo?.isDefaultFav == false) {
                                myItem {
                                    key = MenuKeys.delete
                                    title = "删除收藏夹"
                                }
                            }
                        }
                    }
                }
                myItem {
                    key = MenuKeys.search
                    action = MenuActions.search
                    iconFileName = "ic_search_gray"
                    title = "搜索"
                }
                if (detailInfo != null && !selfFav) {
                    myItem {
                        key = MenuKeys.follow
                        if (detailInfo?.fav_state == 1) {
                            iconFileName = "ic_baseline_favorite_24"
                            title = "已订阅"
                        } else {
                            iconFileName = "ic_outline_favorite_border_24"
                            title = "订阅"
                        }
                    }
                }
            }
        },
        search = SearchConfigInfo(
            name = "搜索${mediaTitle}",
        )
    )

    val scope = rememberCoroutineScope()
    var showEditDialog by remember {
        mutableStateOf(false)
    }
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    PageListener(
        pageConfigId,
        onMenuItemClick = remember(detailInfo) {
            { _, item ->
                when (item.key) {
                    MenuKeys.follow -> {
                        if (detailInfo?.fav_state == 1) {
                            viewModel.unfavFolder()
                        } else {
                            viewModel.favFolder()
                        }
                    }

                    MenuKeys.edit -> {
                        showEditDialog = true
                    }

                    MenuKeys.delete -> {
                        showDeleteDialog = true
                    }

                    MenuKeys.playList -> {
                        viewModel.addPlayList()
                        viewModel.toPlayListPage()
                    }
                }
            }
        },
        onSearchSelfPage = viewModel::searchSelfPage,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TitleBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp + windowInsets.topDp.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(top = windowInsets.topDp.dp),
            icon = {
                if (showTowPane) {
                    IconButton(
                        onClick = {
                            onChangeHideFirstPane(!hideFirstPane)
                        }
                    ) {
                        Icon(
                            imageVector = if (hideFirstPane) {
                                BilimiaoIcons.Common.Menufold
                            } else {
                                BilimiaoIcons.Common.Menuunfold
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp),
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(16.dp))
                }
            },
            title = {
                Text(
                    text = mediaTitle,
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            action = {
                Text(
                    text = "自动连播",
                    style = MaterialTheme.typography.labelMedium,
                )
                Switch(
                    modifier = Modifier.scale(0.75f),
                    checked = isAutoPlay,
                    onCheckedChange = viewModel::changeAutoPlay,
                )
            }
        )
        SwipeToRefresh(
            modifier = Modifier.weight(1f),
            refreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(300.dp),
            ) {
                items(list) {
                    VideoItemBox(
                        modifier = Modifier.padding(10.dp),
                        title = it.title,
                        pic = it.cover,
                        upperName = it.upper.name,
                        playNum = it.cnt_info.play,
                        damukuNum = it.cnt_info.danmaku,
                        duration = NumberUtil.converDuration(it.duration),
                        onClick = {
                            viewModel.openVideo(it)
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

    val info = detailInfo
    if (showEditDialog && info != null) {
        val formState = remember {
            FavouriteEditFormState(
                initialTitle = info.title,
                initialIntro = info.intro,
                initialPrivacy = info.privacy,
            )
        }
        var loading by remember {
            mutableStateOf(false)
        }
        val handleSubmit = remember(info) {
            {
                scope.launch(Dispatchers.IO) {
                    runCatching {
                        loading = true
                        viewModel.editFolder(
                            cover = info.cover,
                            title = formState.title,
                            intro = formState.intro,
                            privacy = formState.privacy,
                        )
                    }.onSuccess {
                        loading = false
                        PopTip.show("修改成功")
                        showEditDialog = false
                        onRefresh()
                    }.onFailure {
                        loading = false
                        PopTip.show(it.message ?: it.toString())
                    }
                }
                Unit
            }
        }

        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
            },
            title = {
                Text(
                    text = "编辑收藏夹",
                    fontWeight = FontWeight.W700,
                    style = MaterialTheme.typography.titleSmall
                )
            },
            text = {
                FavouriteEditForm(formState)
            },
            confirmButton = {
                TextButton(
                    enabled = !loading,
                    onClick = handleSubmit,
                ) {
                    Text(text = "修改")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                    },
                ) {
                    Text(text = "取消")
                }
            }
        )
    } else if (showDeleteDialog) {
        var loading by remember {
            mutableStateOf(false)
        }

        fun handleDelete() = scope.launch(Dispatchers.IO) {
            runCatching {
                loading = true
                viewModel.deleteFolder()
                onRefresh()
                onClose()
            }.onSuccess {
                loading = false
                PopTip.show("修改成功")
                showEditDialog = false
            }.onFailure {
                loading = false
                PopTip.show(it.message ?: it.toString())
            }
        }
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text(
                    text = "提示",
                    fontWeight = FontWeight.W700,
                    style = MaterialTheme.typography.titleSmall
                )
            },
            text = {
                Text(text = "确定删除收藏夹：${mediaTitle}?")
            },
            confirmButton = {
                TextButton(
                    enabled = !loading,
                    onClick = ::handleDelete,
                ) {
                    Text(text = "删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    },
                ) {
                    Text(text = "取消")
                }
            }
        )
    }
}