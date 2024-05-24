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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import cn.a10miaomiao.bilimiao.compose.comm.navigation.openSearch
import cn.a10miaomiao.bilimiao.compose.commponents.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.commponents.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.commponents.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouriteViewModel
import cn.a10miaomiao.bilimiao.compose.pages.user.poup_menu.MyFollowMorePopupMenu
import cn.a10miaomiao.bilimiao.compose.pages.user.poup_menu.UserFavouriteMorePopupMenu
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaDetailInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediasInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.store.WindowStore
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
                list.finished.value = false
                list.fail.value = ""
                list.data.value = listOf()
                loadData(1)
            }
        }
    var mediaTitle: String = ""

    var mediaInfo: MediaListInfo? = null

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
                mediaInfo = result.info
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

    fun menuItemClick(view: View, item: MenuItemPropInfo) {
        when (item.key) {
            MenuKeys.search -> {
                view.openSearch(
                    mode = 1,
                    keyword = "",
                    name = "搜索${mediaTitle}",
                )
            }
            MenuKeys.more -> {
                val pm = UserFavouriteMorePopupMenu(
                    fragment.requireActivity(),
                    parentViewModel,
                    userStore,
                    mediaInfo ?: return,
                )
                pm.show(view)
            }
        }
    }

    fun searchSelfPage(text: String) {
        val nav = fragment.findNavController()
        val url = "bilimiao://user/fav/detail?id=${mediaId}&name=${mediaTitle}&keyword=${text}"
        nav.navigate(Uri.parse(url))
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
        menus = remember {
            listOf(
                myMenuItem {
                    key = MenuKeys.search
                    iconFileName = "ic_search_gray"
                    title = "搜索"
                },
                myMenuItem {
                    key = MenuKeys.more
                    iconFileName = "ic_more_vert_grey_24dp"
                    title = "更多"
                }
            )
        },
        search = SearchConfigInfo(
            name = "搜索${mediaTitle}",
        )
    )
    PageListener(
        pageConfigId,
        onMenuItemClick = viewModel::menuItemClick,
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
}