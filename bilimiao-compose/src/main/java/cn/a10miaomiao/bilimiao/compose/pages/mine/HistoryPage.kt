package cn.a10miaomiao.bilimiao.compose.pages.mine

import android.content.Context
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
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
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState
import cn.a10miaomiao.bilimiao.compose.components.layout.sticky.StickyHeaders
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.components.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouriteDetailPage
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuActions
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
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
        BoxWithConstraints {
            HistoryPageContent(viewModel, maxWidth)
        }
    }
}

private class HistoryPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    @Stable
    class HistoryItem(
        val localDate: LocalDate,
        val item: CursorItem?, // 数据为空时表示为日期分割线
    )

    private val pageNavigation by instance<PageNavigation>()
    private val messageDialog by instance<MessageDialogState>()

    var keyword = ""

    val isRefreshing = MutableStateFlow(false)
    val list = FlowPaginationInfo<HistoryItem>()
    private val _selectedItemMap = mutableStateMapOf<Long, Int>()
    val selectedItemMap: Map<Long, Int> get() = _selectedItemMap

    private var _mapTp = 3
    private var _maxId = 0L
    private var _viewAt = 0L

    init {
        loadData(0L)
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

    fun getDateByCursorItem(item: CursorItem): LocalDate {
        return Instant.fromEpochMilliseconds(item.viewAt * 1000)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    }

    private fun loadData(
        maxId: Long = _maxId
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val keywordText = keyword
            val itemList = if (keywordText.isBlank()) {
                loadList(maxId)
            } else {
                searchList(keywordText, maxId + 1)
            }
            val newListData = if (maxId == 0L) {
                mutableListOf<HistoryItem>()
            } else {
                list.data.value.toMutableList()
            }
            var prevItem = newListData.lastOrNull()
            itemList.forEach {
                val localData = getDateByCursorItem(it)
                if (prevItem?.localDate != localData) {
                    // 添加日期分割
                    newListData.add(
                        HistoryItem(
                            item = null,
                            localDate = localData,
                        )
                    )
                }
                prevItem = HistoryItem(
                    item = it,
                    localDate = localData,
                ).also(newListData::add)
            }
            list.data.value = newListData
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
    ): List<CursorItem>{
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
        res.cursor?.let {
            _maxId = it.max
            _mapTp = it.maxTp
        }
        list.finished.value = !res.hasMore
        return res.items
    }

    private suspend fun searchList(
        keywordText: String,
        pageNum: Long,
    ): List<CursorItem> {
        val req = bilibili.app.interfaces.v1.SearchReq(
            business = "archive",
            keyword = keywordText,
            pn = pageNum,
        )
        val res = BiliGRPCHttp.request {
            HistoryGRPC.search(req)
        }.awaitCall()
        _maxId = res.page?.pn ?: 0
        list.finished.value = !res.hasMore
        return res.items
    }

    fun deleteHistory(kids: Set<Long>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            messageDialog.loading("操作请求中")
            val deleteItems = mutableListOf<CursorItem>()
            val newItems = mutableListOf<HistoryItem>()
            list.data.value.forEach {
                val item = it.item
                if (item != null && kids.indexOf(item.kid) != -1) {
                    deleteItems.add(item)
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
            PopTip.show("已删除选中的${deleteItems.size}个记录")
            clearSelectedItemMap()
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

    fun searchSelfPage(text: String) {
        keyword = text
        _selectedItemMap.clear()
        refreshList()
    }
}


@Composable
private fun HistoryPageContent(
    viewModel: HistoryPageViewModel,
    pageWidth: Dp,
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
                viewModel.clearSelectedItemMap()
                enableEditMode.value = true
            }
            MenuKeys.delete -> {
                val selectedKeys = viewModel.selectedItemMap.keys
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
        title = if (viewModel.keyword.isBlank()) "历史记录"
            else "搜索历史\n-\n${viewModel.keyword}",
        menu = rememberMyMenu(enableEditMode.value) {
            if (enableEditMode.value) {
                myItem {
                    key = MenuKeys.complete
                    title = "完成编辑"
                    iconFileName = "ic_baseline_check_24"
                }
                myItem {
                    key = MenuKeys.delete
                    title = "删除选中"
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
                            title = "批量管理"
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
        },
        search = SearchConfigInfo(
            name = "搜索历史记录",
            keyword = viewModel.keyword,
        )
    )
    PageListener(
        configId = pageConfigId,
        onMenuItemClick = ::menuItemClick,
        onSearchSelfPage = viewModel::searchSelfPage
    )
    BackHandler(
        enabled = enableEditMode.value,
        onBack = {
            enableEditMode.value = false
        }
    )

    val listFlow = viewModel.list
    val list by listFlow.data.collectAsState()

    val scope = rememberCoroutineScope()
    val listState = rememberLazyGridState()
    val calendarListState = rememberLazyListState()
    val sideTimeline = pageWidth >= 650.dp

    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    val currentDate = remember {
        mutableStateOf(today)
    }

    LaunchedEffect(listState, calendarListState) {
        launch {
            snapshotFlow { listState.firstVisibleItemIndex }
                .collectLatest {
                    if (list.size > it) {
                        val itemDate = list[it].localDate
                        calendarListState.animateScrollToItem(
                            itemDate.daysUntil(today)
                        )
                        currentDate.value = itemDate
                    }
                }
        }
    }

    fun scrollToDate(date: LocalDate) {
        scope.launch {
            val index = withContext(Dispatchers.IO) {
                list.indexOfFirst {
                    it.localDate == date
                }
            }
            if (index != -1) {
                listState.scrollToItem(index)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(windowInsets.toPaddingValues(
                bottom = 0.dp
            ))
    ) {
        CalendarRowView(
            modifier = Modifier
                .fillMaxWidth(),
            listState = calendarListState,
            startDate = today,
            endDate = list.lastOrNull()?.localDate,
            currentDate = currentDate.value,
            onChangeDate = ::scrollToDate,
        )
        HistoryListView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            viewModel = viewModel,
            bottomEdgePadding = windowInsets.bottomDp.dp,
            sideTimeline = sideTimeline,
            listState = listState,
            enableEdit = enableEditMode.value,
        )
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

private object LocalDayOfWeekNames {
    val CHINESE_ABBREVIATED: DayOfWeekNames = DayOfWeekNames(
        listOf(
            "一", "二", "三", "四", "五", "六", "日"
        )
    )

    val CHINESE_FULL: DayOfWeekNames = DayOfWeekNames(
        listOf(
            "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"
        )
    )
}

@Composable
private fun CalendarRowView(
    modifier: Modifier,
    listState: LazyListState,
    startDate: LocalDate,
    endDate: LocalDate?,
    currentDate: LocalDate,
    onChangeDate: (LocalDate) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        StickyHeaders(
            modifier = Modifier.fillMaxWidth(),
            state = listState,
            key = { item ->
                val date = startDate.minus(item.index, DateTimeUnit.DAY)
                LocalDate(date.year, date.month, 1)
            },
        ) {
            val formatter = LocalDate.Format {
                year()
                chars("年")
                monthNumber()
                chars("月")
            }
            Text(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                text = it.key.format(formatter),
                style = MaterialTheme.typography.labelSmall,
            )
        }
        LazyRow(
            state = listState,
        ) {
            items(
                count = Int.MAX_VALUE,
                key = { it },
            ) {
                val date = startDate.minus(it, DateTimeUnit.DAY)

                val formatter = LocalDate.Format {
                    dayOfWeek(LocalDayOfWeekNames.CHINESE_ABBREVIATED)
                }

                val dateHeader = formatter.format(date).let { day ->
                    day.firstOrNull()?.toString() ?: day
                }

                val isEnable = date >= (endDate ?: startDate)
                val color = if (isEnable)
                    MaterialTheme.colorScheme.onBackground
                else
                    MaterialTheme.colorScheme.outlineVariant
                Column(
                    modifier = Modifier
                        .size(40.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = dateHeader,
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                    )
                    if (date == currentDate) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = CircleShape,
                                )
                                .size(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${date.dayOfMonth}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondary,
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .run {
                                    if (isEnable) clickable { onChangeDate(date) }
                                    else this
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                modifier = Modifier,
                                text = "${date.dayOfMonth}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = color,
                            )
                        }
                    }

                }
            }
        }
    }
}



@Composable
private fun HistoryListView(
    modifier: Modifier,
    viewModel: HistoryPageViewModel,
    bottomEdgePadding: Dp,
    sideTimeline: Boolean,
    listState: LazyGridState,
    enableEdit: Boolean,
) {
    val listFlow = viewModel.list
    val list by listFlow.data.collectAsState()
    val listLoading by listFlow.loading.collectAsState()
    val listFinished by listFlow.finished.collectAsState()
    val listFail by listFlow.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    SwipeToRefresh(
        modifier = modifier,
        refreshing = isRefreshing,
        onRefresh = viewModel::refreshList,
    ) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            columns = GridCells.Adaptive(300.dp),
            contentPadding = PaddingValues(
                start = if (sideTimeline) {
                    50.dp
                } else {
                    0.dp
                }
            )
        ) {
            items(
                list.size,
                span = {
                    if (list[it].item == null) {
                        GridItemSpan(maxLineSpan)
                    } else {
                        GridItemSpan(1)
                    }
                },
                contentType = { if (list[it].item == null) 0 else 1 }
            ) { index ->
                val item = list[index].item
                if (item == null) {
                    Spacer(modifier = Modifier.height(if (sideTimeline) 10.dp else 30.dp))
                } else {
                    val isChecked = enableEdit && viewModel.selectedItemMap.containsKey(item.kid)
                    val duration = item.cardOgv?.duration ?: item.cardUgc?.duration ?: 0
                    val progress = item.cardUgc?.progress ?: item.cardUgc?.progress ?: 0
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
                            pic = item.cardOgv?.cover
                                ?: item.cardUgc?.cover,
                            upperName = item.cardUgc?.name,
                            remark = NumberUtil.converCTime(item.viewAt),
                            duration = if (progressRatio >= 0.95f) {
                                "已看完"
                            } else if (progressRatio > 0f) {
                                "${NumberUtil.converDuration(progress)}/${NumberUtil.converDuration(duration)}"
                            } else {
                                NumberUtil.converDuration(duration)
                            },
                            progress = progressRatio,
                            isHtml = true,
                            onClick = {
                                if (!enableEdit) {
                                    viewModel.toVideoDetail(item)
                                } else if (isChecked) {
                                    viewModel.removeSelectedItem(item.kid)
                                } else {
                                    viewModel.addSelectedItem(item.kid, index)
                                }
                            }
                        )
                        if (enableEdit) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    if (it) {
                                        viewModel.addSelectedItem(item.kid, index)
                                    } else {
                                        viewModel.removeSelectedItem(item.kid)
                                    }
                                }
                            )
                        }

                    }
                }
            }
            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                ListStateBox(
                    modifier = Modifier.padding(
                        bottom = bottomEdgePadding
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

        StickyHeaders(
            modifier = Modifier
                .fillMaxHeight(),
            state = listState,
            key = { item ->
                item.firstOrNull()?.let {
                    list.getOrNull(it.index)?.localDate
                }
            },
        ) {
            if (sideTimeline) {
                Column(
                    modifier = Modifier
                        .width(50.dp)
                        .padding(top = 10.dp, end = 5.dp)
                        .background(MaterialTheme.colorScheme.background),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center,
                ) {
                    val formatter = LocalDate.Format {
                        dayOfWeek(LocalDayOfWeekNames.CHINESE_FULL)
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape,
                            )
                            .size(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = 2.dp),
                            textAlign = TextAlign.Center,
                            text = "${it.key.dayOfMonth}",
                            color = MaterialTheme.colorScheme.surface
                        )
                    }
                    Text(
                        modifier = Modifier.padding(top = 5.dp),
                        text = it.key.format(formatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            } else {
                val formatter = LocalDate.Format {
                    monthNumber()
                    chars("-")
                    dayOfMonth()
                    chars(" ")
                    dayOfWeek(LocalDayOfWeekNames.CHINESE_FULL)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = CircleShape,
                            )
                            .size(10.dp),
                    )
                    Text(
                        text = it.key.format(formatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

        }

    }
}