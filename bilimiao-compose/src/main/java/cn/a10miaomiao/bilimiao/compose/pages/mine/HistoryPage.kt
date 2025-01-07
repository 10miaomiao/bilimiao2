package cn.a10miaomiao.bilimiao.compose.pages.mine

import android.content.Context
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import bilibili.app.interfaces.v1.Cursor
import bilibili.app.interfaces.v1.CursorItem
import bilibili.app.interfaces.v1.CursorV2Req
import bilibili.app.interfaces.v1.HistoryGRPC
import cn.a10miaomiao.bilimiao.compose.BilimiaoPageRoute
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
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuActions
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.UserStore
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
class HistoryPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: HistoryPageViewModel = diViewModel()
        HistoryPageContent(viewModel)
    }
}

private class HistoryPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val pageNavigation by instance<PageNavigation>()
    private val messageDialog by instance<MessageDialogState>()

    var keyword = ""

    val isRefreshing = MutableStateFlow(false)
    val list = FlowPaginationInfo<CursorItem>()

    private var _mapTp = 3
    private var _maxId = 0L
    private var _viewAt = 0L

    init {
        loadData(0L)
    }

    private fun loadData(
        maxId: Long = _maxId
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val keywordText = keyword
            if (keywordText.isBlank()) {
                loadList(maxId)
            } else {
                searchList(keywordText, maxId + 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = e.message ?: e.toString()
        } finally {
            list.loading.value = false
            isRefreshing.value = false
        }
    }

    private suspend fun loadList(
        maxId: Long,
    ) {
        val req = CursorV2Req(
            business = "archive",
            cursor = if (maxId != 0L) {
                Cursor(
                    max = maxId,
                    maxTp = _mapTp, // 本页最大值游标类型
                )
            } else {
                Cursor()
            }
        )
        val res = BiliGRPCHttp.request {
            HistoryGRPC.cursorV2(req)
        }.awaitCall()
        val itemList = res.items
        if (maxId == 0L) {
            list.data.value = itemList
        } else {
            list.data.value = list.data.value
                .toMutableList()
                .apply { addAll(itemList) }
        }
        res.cursor?.let {
            _maxId = it.max
            _mapTp = it.maxTp
        }
        list.finished.value = !res.hasMore
    }

    private suspend fun searchList(
        keywordText: String,
        pageNum: Long,
    ) {
        val req = bilibili.app.interfaces.v1.SearchReq(
            business = "archive",
            keyword = keywordText,
            pn = pageNum,
        )
        val res = BiliGRPCHttp.request {
            HistoryGRPC.search(req)
        }.awaitCall()
        val itemList = res.items
        if (pageNum == 1L) {
            list.data.value = itemList
        } else {
            list.data.value = list.data.value
                .toMutableList()
                .apply { addAll(itemList) }
        }
        _maxId = res.page?.pn ?: 0
        list.finished.value = !res.hasMore
    }

    fun deleteHistory(kids: Set<Long>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            messageDialog.loading("操作请求中")
            val deleteItems = mutableListOf<CursorItem>()
            val newItems = mutableListOf<CursorItem>()
            list.data.value.forEach {
                if (kids.indexOf(it.kid) != -1) {
                    deleteItems.add(it)
                } else {
                    newItems.add(it)
                }
            }
            val req = bilibili.app.interfaces.v1.DeleteReq(
                hisInfo = deleteItems.map {
                    bilibili.app.interfaces.v1.HisInfo(
                        business = it.business,
                        kid = it.kid,
                    )
                }
            )
            BiliGRPCHttp.request {
                HistoryGRPC.delete(req)
            }.awaitCall()
            list.data.value = newItems
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show("删除失败:$e")
        } finally {
            messageDialog.close()
        }
    }

    fun clearHistoryList() = viewModelScope.launch(Dispatchers.IO) {
        try {
            messageDialog.loading("操作请求中")
            val req = bilibili.app.interfaces.v1.ClearReq(
                business = "archive"
            )
            BiliGRPCHttp.request {
                HistoryGRPC.clear(req)
            }.awaitCall()
            list.data.value = listOf()
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show("操作失败:$e")
        } finally {
            messageDialog.close()
        }
    }

    private fun tryAgainLoadData() {
        loadData()
    }

    fun loadMore() {
        if (!list.finished.value && !list.loading.value) {
            loadData(_maxId)
        }
    }

    fun refreshList() {
        isRefreshing.value = true
        list.reset()
        loadData(0L)
    }

    fun toVideoDetail(item: CursorItem) {
        when(item.business) {
            "archive" -> {
                pageNavigation.navigateToVideoInfo(item.oid.toString())
            }
            "pgc" -> {
                pageNavigation.navigate(BangumiDetailPage(
                    id = item.kid.toString()
                ))
            }
            else -> {
                PopTip.show("未知类型:${item.business}")
            }
        }

    }
}


@Composable
private fun HistoryPageContent(
    viewModel: HistoryPageViewModel
) {

    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val showClearTipsDialog = remember {
        mutableStateOf(false)
    }
    val enableEditMode = remember {
        mutableStateOf(false)
    }
    val selectedItemsMap = remember {
        mutableStateMapOf<Long, Int>()
    }

    fun clearHistoryList() {
        showClearTipsDialog.value = false
        viewModel.clearHistoryList()
    }

    fun menuItemClick (view: View, menuItem: MenuItemPropInfo) {
        when(menuItem.key) {
            MenuKeys.clear -> {
                showClearTipsDialog.value = true
            }
            MenuKeys.edit -> {
                selectedItemsMap.clear()
                enableEditMode.value = true
            }
            MenuKeys.delete -> {
                val selectedKeys = selectedItemsMap.keys
                if (selectedKeys.isEmpty()) {
                    PopTip.show("未选中任何视频")
                } else {
                    viewModel.deleteHistory(selectedKeys)
                }
            }
            MenuKeys.complete -> {
                enableEditMode.value = false
            }
        }
    }
    val pageConfigId = PageConfig(
        title = "历史记录",
        menu = rememberMyMenu(enableEditMode.value) {
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
                            key = MenuKeys.edit
                            title = "多选操作"
                            iconFileName = "ic_baseline_edit_note_24"
                        }
                        myItem {
                            key = MenuKeys.clear
                            title = "清空历史记录"
                        }
                    }
                }
                myItem {
                    key = MenuKeys.search
                    action = MenuActions.search
                    title = "搜索"
                    iconFileName = "ic_search_gray"
                }
            }
        }
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

    val listFlow = viewModel.list
    val list by listFlow.data.collectAsState()
    val listLoading by listFlow.loading.collectAsState()
    val listFinished by listFlow.finished.collectAsState()
    val listFail by listFlow.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    SwipeToRefresh(
        refreshing = isRefreshing,
        onRefresh = viewModel::refreshList,
    ) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Adaptive(300.dp),
            contentPadding = windowInsets.toPaddingValues()
        ) {
            items(list) {
                VideoItemBox(
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                    title = it.title,
                    pic = it.cardOgv?.cover
                        ?: it.cardUgc?.cover,
                    upperName = it.cardUgc?.name,
                    remark = NumberUtil.converCTime(it.viewAt),
                    duration = NumberUtil.converDuration(
                        it.cardOgv?.duration ?: it.cardUgc?.duration ?: 0
                    ),
                    isHtml = true,
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

    if (showClearTipsDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showClearTipsDialog.value = false
            },
            title = {
                Text(text = "提示")
            },
            text = {
                Text(text = "确认清空历史记录(⊙ˍ⊙)？")
            },
            confirmButton = {
                TextButton(onClick = ::clearHistoryList) {
                    Text(text = "确认")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showClearTipsDialog.value = false
                }) {
                    Text(text = "取消")
                }
            }
        )
    }
}