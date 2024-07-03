package cn.a10miaomiao.bilimiao.compose.pages.user.content

import android.net.Uri
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import bilibili.app.view.v1.ViewGRPC
import cn.a10miaomiao.bilimiao.compose.assets.BilimiaoIcons
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.Common
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Menufold
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Menuunfold
import cn.a10miaomiao.bilimiao.compose.base.navigate
import cn.a10miaomiao.bilimiao.compose.comm.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.comm.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.comm.navigation.findComposeNavController
import cn.a10miaomiao.bilimiao.compose.commponents.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.commponents.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.commponents.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.playlist.PlayListPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouriteViewModel
import cn.a10miaomiao.bilimiao.compose.pages.user.commponents.TitleBar
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListInfo
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListItemInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

private class UserSeasonDetailViewModel(
    override val di: DI,
    private val sid: String,
    private val parentViewModel: UserFavouriteViewModel,
) : ViewModel(), DIAware {

    val fragment: Fragment by instance()
    val userStore: UserStore by instance()
    private val playerDelegate: BasePlayerDelegate by instance()
    private val playerStore by instance<PlayerStore>()
    private val playListStore by instance<PlayListStore>()

    var seasonInfo = MutableStateFlow<bilibili.app.view.v1.UgcSeason?>(null)
    val isRefreshing = MutableStateFlow(false)
    val curSection = MutableStateFlow<bilibili.app.view.v1.Section?>(null)
    val sections = MutableStateFlow<List<bilibili.app.view.v1.Section>>(listOf())
    val list = FlowPaginationInfo<bilibili.app.view.v1.Episode>()
    val isAutoPlay = MutableStateFlow(false)

    init {
        loadData(1)
    }

    private fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val req = bilibili.app.view.v1.SeasonReq(
                seasonId = sid.toLong(),
            )
            val res = BiliGRPCHttp.request {
                ViewGRPC.season(req)
            }.awaitCall()
            seasonInfo.value = res.season
            sections.value = res.season?.sections ?: listOf()
            if (sections.value.isNotEmpty()) {
                setCurrentSection(sections.value[0])
            }
            list.finished.value = true
            list.pageNum = pageNum
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

    fun changeAutoPlay(value: Boolean) {
        isAutoPlay.value = value
    }

    fun openVideo(item: bilibili.app.view.v1.Episode) {
        if (isAutoPlay.value) {
            addPlayList()
            val id = item.cid.toString()
            if (playerStore.state.cid != id) {
                playerDelegate.openPlayer(
                    VideoPlayerSource(
                        mainTitle = item.title,
                        title = item.title,
                        coverUrl = item.cover,
                        aid = item.aid.toString(),
                        id = id,
                        ownerId = item.author?.mid.toString(),
                        ownerName = item.author?.name.toString(),
                    )
                )
            }
        } else {
            fragment.findNavController()
                .navigate(
                    Uri.parse("bilimiao://video/" + item.aid),
                    defaultNavOptions,
                )
        }
    }

    fun setCurrentSection(section: bilibili.app.view.v1.Section) {
        curSection.value = section
        list.data.value = section.episodes
    }

    fun addPlayList() {
        val season = seasonInfo.value
        if (season == null) {
            PopTip.show("数据加载中，请稍后再试")
            return
        }
        val currentId = curSection.value?.id
        val index = sections.value.indexOfFirst {
            currentId == it.id
        }
        playListStore.setPlayList(season, index)
    }

    fun toPlayListPage() {
        val nav = fragment.findComposeNavController()
        nav.navigate(PlayListPage())
    }

    fun favSeason() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = BiliApiService.userApi
                .favFavSeason(
                    seasonId = sid,
                )
                .awaitCall()
                .gson<MessageInfo>(isLog = true)
            if (res.isSuccess) {
                PopTip.show("订阅成功")
//                refresh()
                parentViewModel.updateOpenedSeason(
                    seasonId = sid,
                    favState = 1,
                )
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show(e.message ?: e.toString())
        }
    }

    fun unfavSeason() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = BiliApiService.userApi
                .favUnfavSeason(
                    seasonId = sid,
                )
                .awaitCall()
                .gson<MessageInfo>(isLog = true)
            if (res.isSuccess) {
                PopTip.show("已取消订阅")
//                refresh()
                parentViewModel.updateOpenedSeason(
                    seasonId = sid,
                    favState = 0,
                )
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show(e.message ?: e.toString())
        }
    }

    fun menuItemClick(view: View, item: MenuItemPropInfo) {
        when (item.key) {
            MenuKeys.playList -> {
                addPlayList()
                toPlayListPage()
            }
            MenuKeys.follow -> {
                if (item.action == "fav") {
                    favSeason()
                } else {
                    unfavSeason()
                }
            }
        }
    }
}

@Composable
internal fun UserSeasonDetailContent(
    seasonId: String,
    seasonTitle: String,
    showTowPane: Boolean,
    hideFirstPane: Boolean,
    onChangeHideFirstPane: (hidden: Boolean) -> Unit,
    favState: Int,
) {
    val parentViewModel: UserFavouriteViewModel by rememberInstance()
    val viewModel = diViewModel(
        key = seasonId + parentViewModel.hashCode(),
    ) {
        UserSeasonDetailViewModel(it, seasonId, parentViewModel)
    }
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isAutoPlay by viewModel.isAutoPlay.collectAsState()

    val detailInfo by viewModel.seasonInfo.collectAsState()
    val sections by viewModel.sections.collectAsState()
    val curSection by viewModel.curSection.collectAsState()


    val pageConfigId = PageConfig(
        title = "合集详情",
        menu = rememberMyMenu(favState) {
            myItem {
                key = MenuKeys.more
                iconFileName = "ic_more_vert_grey_24dp"
                title = "更多"
                childMenu = myMenu {
                    myItem {
                        key = MenuKeys.playList
                        title = "添加到播放列表"
                    }
                }
            }
            myItem {
                key = MenuKeys.follow
                if (favState == 1) {
                    action = "unfav"
                    iconFileName = "ic_baseline_favorite_24"
                    title = "已订阅"
                } else {
                    action = "fav"
                    iconFileName = "ic_outline_favorite_border_24"
                    title = "订阅"
                }
            }
        },
    )
    PageListener(
        pageConfigId,
        onMenuItemClick = viewModel::menuItemClick,
        onSearchSelfPage = null
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
                    text = seasonTitle,
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
        if (sections.size > 1) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                items(sections, { it.id }) {
                    FilterChip(
                        selected = curSection?.id == it.id,
                        onClick = {
                            viewModel.setCurrentSection(it)
                        },
                        label = {
                            Text(text = it.title)
                        }
                    )
                }
            }
        }
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
                        title = it.title,
                        pic = it.cover,
                        upperName = it.author?.name,
                        playNum = it.stat?.view.toString(),
                        damukuNum = it.stat?.danmaku.toString(),
                        duration = NumberUtil.converDuration(it.page?.duration ?: 0),
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
}