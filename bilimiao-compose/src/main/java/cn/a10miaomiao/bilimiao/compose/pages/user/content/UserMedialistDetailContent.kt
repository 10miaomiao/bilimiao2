package cn.a10miaomiao.bilimiao.compose.pages.user.content

import android.net.Uri
import android.view.View
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.view.v1.Episode
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
import cn.a10miaomiao.bilimiao.compose.common.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.components.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.playlist.PlayListPage
import cn.a10miaomiao.bilimiao.compose.pages.user.components.TitleBar
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListV2Info
import com.a10miaomiao.bilimiao.comm.entity.media.MediaResponseV2Info
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
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
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

private class UserMedialistDetailViewMode(
    override val di: DI,
    private val bizId: String,
    private val bizType: String,
    private val bizTitle: String,
) : ViewModel(), DIAware {

    val userStore: UserStore by instance()
    private val pageNavigation: PageNavigation by instance()
    private val playerDelegate: BasePlayerDelegate by instance()
    private val playerStore by instance<PlayerStore>()
    private val playListStore by instance<PlayListStore>()

    val isRefreshing = MutableStateFlow(false)
    val list = FlowPaginationInfo<MediaListV2Info>()
    private var lastOid: String = ""

    private val _isAutoPlay = mutableStateOf(false)
    val isAutoPlay get() = _isAutoPlay.value

    init {
        loadData("")
    }

    private fun loadData(
        oid: String = lastOid
    ) = viewModelScope.launch(Dispatchers.IO) {
        val type = when(bizType) {
            "series" -> "5"
            "season" -> "8"
            else -> {
                PopTip.show("未知类型：$bizType")
                list.fail.value = "未知类型：$bizType"
                return@launch
            }
        }
        try {
            list.loading.value = true
            val res = BiliApiService.userApi
                .medialistResourceList(
                    bizId = bizId,
                    type = type,
                    oid = oid,
                )
                .awaitCall()
                .json<ResultInfo<MediaResponseV2Info>>()
            if (res.isSuccess) {
                val mediaList = res.data.media_list
                if (mediaList != null) {
                    if (oid.isBlank()) {
                        list.data.value = mediaList.toMutableList()
                    } else {
                        val oriList = list.data.value
                        list.data.value = mutableListOf(
                            *oriList.toTypedArray(),
                            *mediaList.filter { i1 ->
                                oriList.indexOfFirst { i2 -> i1.id == i2.id } == -1
                            }.toTypedArray()
                        )
                    }
                }
                lastOid = mediaList?.lastOrNull()?.id ?: ""
                list.finished.value = !res.data.has_more
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

    fun refresh() {
        isRefreshing.value = true
        list.finished.value = false
        list.fail.value = ""
        loadData("")
    }

    fun loadMore() {
        if (!list.finished.value && !list.loading.value) {
            loadData(lastOid)
        }
    }

    fun openVideo(item: MediaListV2Info) {
        if (isAutoPlay) {
            addPlayList()
            if (playerStore.state.cid != item.id) {
                playerDelegate.openPlayer(
                    VideoPlayerSource(
                        mainTitle = item.title,
                        title = item.title,
                        coverUrl = item.cover,
                        aid = item.id,
                        id = item.pages[0].id,
                        ownerId = item.upper.mid,
                        ownerName = item.upper.name,
                    )
                )
            }
        } else {
            pageNavigation.navigateToVideoInfo(item.id)
        }
    }

    fun addPlayList() {
        val type = when(bizType) {
            "series" -> "5"
            "season" -> "8"
            else -> {
                PopTip.show("未知类型：$bizType")
                list.fail.value = "未知类型：$bizType"
                return
            }
        }
        playListStore.setMedialistList(
            bizId = bizId,
            bizType = type,
            bizTitle = bizTitle,
        )
    }

    fun toPlayListPage() {
        pageNavigation.navigate(PlayListPage())
    }

    fun changeAutoPlay(value: Boolean) {
        _isAutoPlay.value = value
    }

    fun menuItemClick(view: View, item: MenuItemPropInfo) {
        when (item.key) {
            MenuKeys.playList -> {
                addPlayList()
                toPlayListPage()
            }
        }
    }
}

@Composable
fun UserMedialistDetailContent(
    bizType: String,
    bizId: String,
    bizTitle: String,
    showTowPane: Boolean,
    hideFirstPane: Boolean,
    onChangeHideFirstPane: (hidden: Boolean) -> Unit,
) {
    val viewModel = diViewModel(
        key = bizType + bizId,
    ) {
        UserMedialistDetailViewMode(
            it,
            bizId = bizId,
            bizType = bizType,
            bizTitle = bizTitle,
        )
    }
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()


    val pageConfigId = PageConfig(
        title = "合集详情",
        menu = rememberMyMenu() {
            myItem {
                key = MenuKeys.more
                iconFileName = "ic_more_vert_grey_24dp"
                title = "更多"
                childMenu = myMenu {
                    myItem {
                        key = MenuKeys.playList
                        title = "设置为播放列表"
                    }
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
                    text = bizTitle,
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
                    checked = viewModel.isAutoPlay,
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
                items(list) { item ->
                    VideoItemBox(
                        modifier = Modifier.padding(10.dp),
                        title = item.title,
                        pic = item.cover,
                        remark = NumberUtil.converCTime(item.pubtime),
                        playNum = item.cnt_info.play.toString(),
                        damukuNum = item.cnt_info.danmaku.toString(),
                        duration = NumberUtil.converDuration(item.duration),
                        onClick = {
                            viewModel.openVideo(item)
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