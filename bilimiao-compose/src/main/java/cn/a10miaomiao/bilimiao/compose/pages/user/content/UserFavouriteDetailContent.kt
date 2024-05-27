package cn.a10miaomiao.bilimiao.compose.pages.user.content

import android.net.Uri
import android.view.View
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.comm.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.commponents.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.commponents.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.commponents.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouriteFolderType
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouriteViewModel
import cn.a10miaomiao.bilimiao.compose.pages.user.commponents.FavouriteEditForm
import cn.a10miaomiao.bilimiao.compose.pages.user.commponents.FavouriteEditFormState
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaDetailInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediasInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuActions
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
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
) : ViewModel(), DIAware {

    val fragment: Fragment by instance()
    val userStore: UserStore by instance()
    val parentViewModel: UserFavouriteViewModel by instance()

    var mediaId: String = ""
        set(value) {
            if (field != value) {
                field = value
                mediaInfo.value = null
                list.finished.value = false
                list.fail.value = ""
                list.data.value = listOf()
                loadData(1)
            }
        }
    var mediaTitle: String = ""

    var mediaInfo = MutableStateFlow<MediaListInfo?>(null)
    val isRefreshing = MutableStateFlow(false)
    val list = FlowPaginationInfo<MediasInfo>()
    val keyword = MutableStateFlow("")


    private fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val res = BiliApiService.userApi.mediaDetail(
                media_id = mediaId,
                keyword = keyword.value,
                pageNum = pageNum,
                pageSize = list.pageSize
            ).awaitCall().gson<ResultInfo<MediaDetailInfo>>()
            if (res.code == 0) {
                val result = res.data
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
                list.finished.value = mediaList.size < list.pageSize
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

    fun toVideoDetailPage(item: MediasInfo) {
        fragment.findNavController()
            .navigate(
                Uri.parse("bilimiao://video/" + item.id),
                defaultNavOptions,
            )
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
            .gson<MessageInfo>()
        if (!res.isSuccess) {
            throw Exception(res.message)
        }
//        parentViewModel.refresh(UserFavouriteFolderType.Created)
        parentViewModel.updateOpenedMedia(
            mediaId = mediaId,
            title = title,
            cover = cover,
            intro = intro,
            privacy = privacy,
        )
        refresh()
    }

    suspend fun deleteFolder() {
        val res = BiliApiService.userApi
            .favDeleteFolder(
                mediaIds = mediaId,
            )
            .awaitCall()
            .gson<MessageInfo>(isLog = true)
        if (!res.isSuccess) {
            throw Exception(res.message)
        }
        parentViewModel.refresh(UserFavouriteFolderType.Created)
        parentViewModel.closeMediaDetail()
    }

    fun favFolder() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = BiliApiService.userApi
                .favFavFolder(
                    mediaId = mediaId,
                )
                .awaitCall()
                .gson<MessageInfo>(isLog = true)
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
                .gson<MessageInfo>(isLog = true)
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
        val nav = fragment.findNavController()
        val url = "bilimiao://user/fav/detail?id=${mediaId}&name=${mediaTitle}&keyword=${text}"
        nav.navigate(Uri.parse(url))
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
) {
    val viewModel: UserFavouriteDetailViewModel = diViewModel()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val detailInfo by viewModel.mediaInfo.collectAsState()
    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(mediaId) {
        viewModel.mediaId = mediaId
        viewModel.mediaTitle = mediaTitle
    }

    val pageConfigId = PageConfig(
        title = mediaTitle,
        menu = remember(detailInfo) {
            myMenu {
                val selfFav = viewModel.isSelfFav()
                if (detailInfo != null && selfFav) {
                    myItem {
                        key = MenuKeys.more
                        iconFileName = "ic_more_vert_grey_24dp"
                        title = "更多"
                        childMenu = myMenu {
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
                }
            }
        },
        onSearchSelfPage = viewModel::searchSelfPage,
    )

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
                    Text(text = mediaTitle)
                }
                HorizontalDivider(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
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
                    VideoItemBox(
                        title = it.title,
                        pic = it.cover,
                        upperName = it.upper.name,
                        playNum = it.cnt_info.play,
                        damukuNum = it.cnt_info.danmaku,
                        duration = NumberUtil.converDuration(it.duration),
                        onClick = {
                            viewModel.toVideoDetailPage(it)
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
                        PopTip.show("创建成功")
                        showEditDialog = false
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
            }.onSuccess {
                loading = false
                PopTip.show("创建成功")
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