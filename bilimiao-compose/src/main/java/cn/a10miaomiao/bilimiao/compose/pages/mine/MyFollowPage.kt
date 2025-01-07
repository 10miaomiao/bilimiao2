package cn.a10miaomiao.bilimiao.compose.pages.mine

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.foundation.pagerTabIndicatorOffset
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.pages.user.TagEditDialogState
import cn.a10miaomiao.bilimiao.compose.pages.mine.content.TagFollowContent
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.MessageDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.bindSingleton
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.subDI

@Serializable
class MyFollowPage() : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel = diViewModel<MyFollowViewModel>()
        subDI(
            diBuilder = {
                bindSingleton { viewModel }
            }
        ) {
            MyFollowPageContent()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun MyFollowPageContent() {
    val viewModel: MyFollowViewModel by rememberInstance()
    val userStore: UserStore by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(LocalView.current)

    val tagList by viewModel.tagList.data.collectAsState()
    val tagListLoading by viewModel.tagList.loading.collectAsState()
    val tagListFail by viewModel.tagList.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val orderType by viewModel.orderType.collectAsState()
    val orderTypeToNameMap = viewModel.orderTypeToNameMap
    val isLogin = userStore.isLogin()

    val pagerState = rememberPagerState(pageCount = { tagList.size })
    val currentPage = pagerState.currentPage
    val scope = rememberCoroutineScope()

    val pageConfigId = PageConfig(
        title = "我的关注",
        menu = remember(currentPage, tagList, orderType) {
            myMenu {
                if (
                    currentPage in tagList.indices
                    && tagList[currentPage].tagid > 0
                ) {
                    myItem {
                        key = MenuKeys.more
                        iconFileName = "ic_more_vert_grey_24dp"
                        title = "更多"
                        childMenu = myMenu {
                            myItem {
                                key = MenuKeys.edit
                                title = "修改分组"
                            }
                            myItem {
                                key = MenuKeys.delete
                                title = "删除分组"
                            }
                        }
                    }
                }
                myItem {
                    key = MenuKeys.search
                    iconFileName = "ic_search_gray"
                    title = "搜索"
                }
                myItem {
                    key = MenuKeys.filter
                    iconFileName = "ic_baseline_filter_list_grey_24"
                    title = viewModel.orderTypeToNameMap[orderType]
                    childMenu = myMenu {
                        checkable = true
                        checkedKey = orderTypeToNameMap.keys.indexOf(orderType)
                        orderTypeToNameMap.values.forEachIndexed { index, s ->
                            myItem {
                                key = index
                                title = s
                            }
                        }
                    }
                }
            }
        }
    )

    PageListener(
        configId = pageConfigId,
        onMenuItemClick = remember(currentPage, tagList) {
            { _, item ->
                when (item.key) {
                    in orderTypeToNameMap.keys.indices -> {
                        val value = orderTypeToNameMap.keys.elementAt(item.key!!)
                        viewModel.changeOrderType(value)
                    }

                    MenuKeys.search -> {
                        viewModel.toSearchPage()
                    }

                    MenuKeys.edit -> {
                        val tagInfo = tagList[currentPage]
                        viewModel.updateTagEditDialogState(
                            TagEditDialogState.Update(
                                tagInfo.tagid,
                                tagInfo.name,
                            )
                        )
                    }

                    MenuKeys.delete -> {
                        val tagInfo = tagList[currentPage]
                        if (tagInfo.count > 0) {
                            MessageDialog.build()
                                .setTitle("提示")
                                .setMessage("该分组下还有关注的人\n删除后将会放到默认分组")
                                .setOkButton("确定") { _, _ ->
                                    viewModel.deleteTag(tagInfo.tagid)
                                    false
                                }
                                .setCancelButton("取消")
                                .show()
                        } else {
                            viewModel.deleteTag(tagInfo.tagid)
                        }
                    }
                }
                Unit
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            ScrollableTabRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(
                        top = windowInsets.topDp.dp,
                        start = windowInsets.leftDp.dp,
                        end = windowInsets.rightDp.dp,
                    ),
                edgePadding = 0.dp,
                selectedTabIndex = currentPage,
                indicator = { positions ->
                    if (currentPage > -1 && currentPage < positions.size) {
                        TabRowDefaults.PrimaryIndicator(
                            Modifier.pagerTabIndicatorOffset(pagerState, positions),
                        )
                    }
                },
                divider = {}
            ) {
                tagList.forEachIndexed { index, tab ->
                    Tab(
                        text = {
                            Text(
                                text = "${tab.name}(${tab.count})",
                                color = if (index == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                }
                            )
                        },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                    )
                }
            }
            HorizontalDivider(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
            )
        }

        if (tagListLoading) {
            ListStateBox(loading = true)
        } else if (tagListFail.isNotBlank()) {
            ListStateBox(
                fail = tagListFail,
                loadMore = viewModel::tryAgainLoadData
            )
        }
        val saveableStateHolder = rememberSaveableStateHolder()
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = pagerState,
        ) { index ->
            val id = tagList[index].tagid
            saveableStateHolder.SaveableStateProvider(id.toString() + orderType) {
                TagFollowContent(
                    tagId = id,
                    orderType = orderType,
                )
            }
        }

    }

    val tagEditDialogState = viewModel.tagEditDialogState.collectAsState().value
    val userTagSetDialogState = viewModel.userTagSetDialogState.collectAsState().value
    if (userTagSetDialogState != null) {
        val selectedUser = userTagSetDialogState.user
        val selectedTag = remember(selectedUser) {
            val tagPairList = mutableListOf<Pair<Int, Int>>()
            selectedUser.tag?.forEachIndexed { i, tagId ->
                if (tagId != 0) {
                    tagPairList.add(tagId to i)
                }
            }
            if (tagPairList.isEmpty()) {
                tagPairList.add(0 to 0)
            }
            mutableStateMapOf(*tagPairList.toTypedArray())
        }
        AlertDialog(
            onDismissRequest = viewModel::clearUserTagSetDialogState,
            title = {
                Text(
                    text = "设置分组",
                    fontWeight = FontWeight.W700,
                    style = MaterialTheme.typography.titleSmall
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(tagList) { item ->
                        if (item.tagid != 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = selectedTag.containsKey(item.tagid),
                                    onCheckedChange = {
                                        if (selectedTag.containsKey(item.tagid)) {
                                            selectedTag.remove(item.tagid)
                                        } else {
                                            selectedTag[item.tagid] =
                                                selectedUser.tag?.indexOf(item.tagid) ?: -1
                                        }
                                    }
                                )
                                Text(text = item.name)
                            }
                        }
                    }
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateTagEditDialogState(
                                        TagEditDialogState.Add
                                    )
                                }
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp, 48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "添加图标"
                                )
                            }
                            Text(text = "添加分组")
                        }
                    }
                }
            },
            confirmButton = {
                if (selectedTag.isEmpty()) {
                    TextButton(onClick = {
                        viewModel.addUserTags(
                            user = selectedUser,
                            tagIds = listOf(0),
                        )
                        viewModel.clearUserTagSetDialogState()
                    }) {
                        Text(text = "保存至默认分组")
                    }
                } else {
                    TextButton(onClick = {
                        val tagMap = selectedTag.toMap()
                        viewModel.addUserTags(
                            user = selectedUser,
                            tagIds = tagMap.keys.filter { it != 0 },
                        )
                        viewModel.clearUserTagSetDialogState()
                    }) {
                        Text(text = "确认")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::clearUserTagSetDialogState,
                ) {
                    Text(text = "取消")
                }
            }
        )
    }
    if (tagEditDialogState != null) {
        var tagText by remember(tagEditDialogState) {
            if (tagEditDialogState is TagEditDialogState.Update) {
                mutableStateOf(tagEditDialogState.name)
            } else {
                mutableStateOf("")
            }
        }
        var loading by remember {
            mutableStateOf(false)
        }

        fun handleConfirmClick() {
            scope.launch(Dispatchers.IO) {
                loading = true
                when (val state = tagEditDialogState) {
                    is TagEditDialogState.Add -> {
                        viewModel.addTag(tagText)
                    }

                    is TagEditDialogState.Update -> {
                        viewModel.updateTag(state.id, tagText)
                    }

                    null -> Unit
                }
                loading = false
            }
        }
        AlertDialog(
            onDismissRequest = viewModel::clearTagEditDialogState,
            title = {
                Text(
                    text = "分组名称",
                    fontWeight = FontWeight.W700,
                    style = MaterialTheme.typography.titleSmall
                )
            },
            text = {
                TextField(
                    value = tagText,
                    onValueChange = {
                        tagText = it
                    },
                    placeholder = {
                        Text(text = "分组名称不允许以特殊字符命名")
                    }
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !loading,
                    onClick = ::handleConfirmClick,
                ) {
                    when (tagEditDialogState) {
                        is TagEditDialogState.Add -> {
                            Text(text = "添加")
                        }

                        is TagEditDialogState.Update -> {
                            Text(text = "修改")
                        }

                        null -> {
                            Text(text = "提交")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::clearTagEditDialogState,
                ) {
                    Text(text = "取消")
                }
            }
        )
    }

}