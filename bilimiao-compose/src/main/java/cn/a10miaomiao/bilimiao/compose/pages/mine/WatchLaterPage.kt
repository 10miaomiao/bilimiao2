package cn.a10miaomiao.bilimiao.compose.pages.mine

import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.interfaces.v1.CursorItem
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.components.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.playlist.PlayListPage
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.history.ToViewInfo
import com.a10miaomiao.bilimiao.comm.entity.history.ToViewItemInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediasInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuActions
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
class WatchLaterPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: WatchLaterPageViewModel = diViewModel()
        WatchLaterPageContent(viewModel)
    }
}

private class WatchLaterPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val pageNavigation by instance<PageNavigation>()
    private val messageDialog by instance<MessageDialogState>()
    private val playListStore by instance<PlayListStore>()
    private val playerStore by instance<PlayerStore>()
    private val playerDelegate by instance<BasePlayerDelegate>()

    private var nextKey = ""

    private val _listAsc = mutableStateOf(false)
    val listAsc get() = _listAsc.value
    private val _listSortField = mutableIntStateOf(1)
    val listSortField get() = _listSortField.intValue
    private val _isAutoPlay = mutableStateOf(false)
    val isAutoPlay get() = _isAutoPlay.value

    val isRefreshing = MutableStateFlow(false)
    var list = FlowPaginationInfo<ToViewItemInfo>()
    private val _selectedItemMap = mutableStateMapOf<Long, Int>()
    val selectedItemMap: Map<Long, Int> get() = _selectedItemMap

    init {
        loadData("")
    }

    fun clearSelectedItemMap() {
        _selectedItemMap.clear()
    }

    fun addSelectedItem(key: Long, i: Int) {
        _selectedItemMap[key] = i
    }

    fun removeSelectedItem(key: Long) {
        _selectedItemMap.remove(key)
    }

    fun setListAsc(asc: Boolean) {
        _listAsc.value = asc
        refreshList(false)
    }

    fun setListSortField(sortField: Int) {
        _listSortField.intValue = sortField
        refreshList(false)
    }

    fun changeAutoPlay(autoPlay: Boolean) {
        _isAutoPlay.value = autoPlay
    }

    private fun loadData(
        startKey: String = nextKey,
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val res = BiliApiService.userApi
                .videoToview(
                    sortField = listSortField,
                    asc = listAsc,
                    startKey = startKey,
                )
                .awaitCall()
                .json<ResponseData<ToViewInfo>>()
            if (res.code == 0) {
                val data = res.requireData()
                val listData = data.list
                if (startKey.isBlank()) {
                    list.data.value = listData.toMutableList()
                } else {
                    list.data.value = list.data.value
                        .toMutableList()
                        .apply { addAll(listData) }
                }
                nextKey = data.next_key
                list.finished.value = !data.has_more
            } else {
                list.fail.value = res.message
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = e.message ?: e.toString()
        } finally {
            list.loading.value = false
            isRefreshing.value = false
        }
    }

    fun deleteToview(aids: Set<Long>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            messageDialog.loading("操作请求中")
            val deleteItems = mutableListOf<ToViewItemInfo>()
            val newItems = mutableListOf<ToViewItemInfo>()
            list.data.value.forEach {
                if (aids.indexOf(it.aid) != -1) {
                    deleteItems.add(it)
                } else {
                    newItems.add(it)
                }
            }
            val res = BiliApiService.userApi
                .videoToviewDels(aids.map { it.toString() })
                .awaitCall()
                .json<MessageInfo>()
            if (res.code == 0) {
                list.data.value = newItems
                PopTip.show("已移除选中的${deleteItems.size}个视频")
                _selectedItemMap.clear()
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show("失败:$e")
        } finally {
            messageDialog.close()
        }
    }

    fun cleanViewed() = viewModelScope.launch(Dispatchers.IO) {
        try {
            messageDialog.loading("操作请求中")
            val res = BiliApiService.userApi
                .videoToviewClean(2)
                .awaitCall()
                .json<MessageInfo>()
            if (res.code == 0) {
                refreshList()
                PopTip.show("操作成功")
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show("失败:$e")
        } finally {
            messageDialog.close()
        }
    }

    fun cleanExpired() = viewModelScope.launch(Dispatchers.IO) {
        try {
            messageDialog.loading("操作请求中")
            val res = BiliApiService.userApi
                .videoToviewClean(1)
                .awaitCall()
                .json<MessageInfo>()
            if (res.code == 0) {
                refreshList()
                PopTip.show("操作成功")
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show("失败:$e")
        } finally {
            messageDialog.close()
        }
    }

    private fun tryAgainLoadData() {
        loadData()
    }

    fun loadMore() {
        if (!list.finished.value && !list.loading.value) {
            loadData(nextKey)
        }
    }

    fun refreshList(refreshing: Boolean = true) {
        isRefreshing.value = refreshing
        list.reset()
        _selectedItemMap.clear()
        loadData("")
    }

    fun setPlayList() {
        playListStore.setToviewList(listSortField, listAsc)
    }

    fun openVideo(item: ToViewItemInfo) {
        if (isAutoPlay && playerStore.state.aid != item.aid.toString()) {
            playerDelegate.openPlayer(
                VideoPlayerSource(
                    mainTitle = item.title,
                    title = item.title,
                    coverUrl = item.pic,
                    aid = item.aid.toString(),
                    id = item.cid.toString(),
                    ownerId = item.owner.mid,
                    ownerName = item.owner.name,
                )
            )
            setPlayList()
        } else {
            toVideoDetail(item)
        }
    }

    fun toPlaylist() {
        pageNavigation.navigate(PlayListPage())
    }

    fun toVideoDetail(item: ToViewItemInfo) {
        pageNavigation.navigateToVideoInfo(item.aid.toString())
    }
}


@Composable
private fun WatchLaterPageContent(
    viewModel: WatchLaterPageViewModel
) {
    val showCleanViewedTipsDialog = remember {
        mutableStateOf(false)
    }
    val showCleanExpiredTipsDialog = remember {
        mutableStateOf(false)
    }

    val enableEditMode = remember {
        mutableStateOf(false)
    }
    fun menuItemClick (view: View, menuItem: MenuItemPropInfo) {
        when(menuItem.key) {
            1 -> {
                viewModel.setListAsc(false)
            }
            2 -> {
                viewModel.setListAsc(true)
            }
            MenuKeys.playList -> {
                viewModel.setPlayList()
                viewModel.toPlaylist()
            }
            MenuKeys.clear -> {
                showCleanViewedTipsDialog.value = true
            }
            -MenuKeys.clear -> {
                showCleanExpiredTipsDialog.value = true
            }
            MenuKeys.edit -> {
                viewModel.clearSelectedItemMap()
                enableEditMode.value = true
            }
            MenuKeys.delete -> {
                val selectedKeys = viewModel.selectedItemMap.keys
                if (selectedKeys.isEmpty()) {
                    PopTip.show("未选中任何视频")
                } else {
                    viewModel.deleteToview(selectedKeys)
                }
            }
            MenuKeys.complete -> {
                enableEditMode.value = false
            }
        }
    }

    val pageConfigId = PageConfig(
        title = "稍后再看",
        menu = rememberMyMenu(enableEditMode.value, viewModel.listAsc) {
            if (enableEditMode.value) {
                myItem {
                    key = MenuKeys.complete
                    title = "完成编辑"
                    iconFileName = "ic_baseline_check_24"
                }
                myItem {
                    key = MenuKeys.delete
                    title = "移除选中"
                    iconFileName = "ic_baseline_delete_outline_24"
                }
            } else {
                myItem {
                    key = MenuKeys.more
                    title = "更多"
                    iconFileName = "ic_more_vert_grey_24dp"
                    childMenu = myMenu {
                        myItem {
                            key = MenuKeys.playList
                            title = "设置为播放列表"
                        }
                        myItem {
                            key = MenuKeys.edit
                            title = "批量管理"
                        }
                        myItem {
                            key = MenuKeys.clear
                            title = "清除已观看视频"
                        }
                        myItem {
                            key = -MenuKeys.clear
                            title = "清除已失效视频"
                        }
                    }
                }
                myItem {
                    key = MenuKeys.sort
                    title = if (viewModel.listAsc) "最早添加" else "最近添加"
                    iconFileName = "ic_baseline_filter_list_grey_24"
                    childMenu = myMenu {
                        checkable = true
                        checkedKey = if (viewModel.listAsc) 2 else 1
                        myItem {
                            key = 1
                            title = "最近添加"
                        }
                        myItem {
                            key = 2
                            title = "最早添加"
                        }
                    }
                }
            }
        },
    )
    PageListener(
        configId = pageConfigId,
        onMenuItemClick = ::menuItemClick,
    )
    BackHandler(
        enabled = enableEditMode.value,
        onBack = {
            enableEditMode.value = false
        }
    )

    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val listFlow = viewModel.list
    val list by listFlow.data.collectAsState()
    val listLoading by listFlow.loading.collectAsState()
    val listFinished by listFlow.finished.collectAsState()
    val listFail by listFlow.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
    ){
        Row(
            modifier = Modifier
                .padding(windowInsets.toPaddingValues(
                    bottom = 0.dp
                ))
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = viewModel.listSortField == 1,
                onClick = {
                    viewModel.setListSortField(1)
                },
                label = {
                    Text("全部")
                }
            )
            FilterChip(
                selected = viewModel.listSortField == 10,
                onClick = {
                    viewModel.setListSortField(10)
                },
                label = {
                    Text("未看完")
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "自动连播",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Switch(
                modifier = Modifier.scale(0.75f),
                checked = viewModel.isAutoPlay,
                onCheckedChange = viewModel::changeAutoPlay,
            )
        }
        SwipeToRefresh(
            refreshing = isRefreshing,
            onRefresh = viewModel::refreshList,
        ) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(300.dp),
                contentPadding = windowInsets.toPaddingValues(
                    top = 0.dp
                )
            ) {
                items(list.size) { index ->
                    val item = list[index]
                    val enableEdit = enableEditMode.value
                    val isChecked = enableEdit && viewModel.selectedItemMap.containsKey(item.aid)
                    val duration = item.duration
                    val progress = item.progress
                    val progressRatio = if (duration > 0L) progress.toFloat() / duration.toFloat() else 0f
                    Box(
                        contentAlignment = Alignment.CenterStart
                    ) {
                        VideoItemBox(
                            modifier = Modifier
                                .run {
                                    if (enableEdit) alpha(0.6f)
                                    else this
                                }
                                .padding(
                                    horizontal = 10.dp,
                                    vertical = 5.dp
                                ),
                            title = item.title,
                            pic = item.pic,
                            upperName = item.owner.name,
                            duration = if (progressRatio >= 0.95f) {
                                "已看完"
                            } else if (progressRatio > 0f) {
                                "${NumberUtil.converDuration(progress)}/${NumberUtil.converDuration(duration)}"
                            } else {
                                NumberUtil.converDuration(duration)
                            },
                            progress = progressRatio,
                            playNum = item.left_text,
                            damukuNum = item.right_text,
                            onClick = {
                                if (!enableEdit) {
                                    viewModel.openVideo(item)
                                } else if (isChecked) {
                                    viewModel.removeSelectedItem(item.aid)
                                } else {
                                    viewModel.addSelectedItem(item.aid, index)
                                }
                            }
                        )
                        if (enableEdit) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    if (it) {
                                        viewModel.addSelectedItem(item.aid, index)
                                    } else {
                                        viewModel.removeSelectedItem(item.aid)
                                    }
                                }
                            )
                        }
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

    if (showCleanViewedTipsDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showCleanViewedTipsDialog.value = false
            },
            title = {
                Text(text = "提示")
            },
            text = {
                Column {
                    Text(text = "确认移除已观看的视频(⊙ˍ⊙)？")
                    Text(text = "已看完视频即：播放进度达95%")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showCleanViewedTipsDialog.value = false
                    viewModel.cleanViewed()
                }) {
                    Text(text = "确认")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCleanViewedTipsDialog.value = false
                }) {
                    Text(text = "取消")
                }
            }
        )
    } else if (showCleanExpiredTipsDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showCleanExpiredTipsDialog.value = false
            },
            title = {
                Text(text = "提示")
            },
            text = {
                Text(text = "确认移除已失效的视频(⊙ˍ⊙)？")
            },
            confirmButton = {
                TextButton(onClick = {
                    showCleanExpiredTipsDialog.value = false
                    viewModel.cleanExpired()
                }) {
                    Text(text = "确认")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCleanExpiredTipsDialog.value = false
                }) {
                    Text(text = "取消")
                }
            }
        )
    }
}