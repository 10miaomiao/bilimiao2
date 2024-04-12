package cn.a10miaomiao.bilimiao.compose.pages.user

import android.app.Activity
import android.view.View
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.base.navigate
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.comm.localNavController
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageMenuItemClick
import cn.a10miaomiao.bilimiao.compose.comm.navigation.findComposeNavController
import cn.a10miaomiao.bilimiao.compose.commponents.dialogs.SingleChoiceItem
import cn.a10miaomiao.bilimiao.compose.pages.user.content.TagFollowContent
import cn.a10miaomiao.bilimiao.compose.pages.user.poup_menu.UserFollowOrderPopupMenu
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

class MyFollowPage() : ComposePage() {

    override val route: String
        get() = "follow"

    @Composable
    override fun AnimatedContentScope.Content(navEntry: NavBackStackEntry) {
        val viewModel = diViewModel<MyFollowPageViewModel>()
        MyFollowPageContent(viewModel = viewModel)
    }
}

private class MyFollowPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val activity by instance<Activity>()
    private val userStore by instance<UserStore>()

    val count = MutableStateFlow(1)
    val isRefreshing = MutableStateFlow(false)
    val listState = MutableStateFlow(LazyListState(0, 0))
    val tagList = FlowPaginationInfo<TagInfo>()

    val orderTypeList = listOf(
        SingleChoiceItem("最常访问", "attention"),
        SingleChoiceItem("关注顺序", ""),
    )
    val orderType = MutableStateFlow("attention")
    val orderTypeToNameMap = mapOf(
        "attention" to "最常访问",
        "" to "关注顺序",
    )

    fun add() {
        count.value = count.value + 1
    }

//    fun changeOrderType(value: String) {
//        orderType.value = value
//        list.data.value = emptyList()
//        list.finished.value = false
//        list.fail.value = ""
//        loadData(1)
//    }

    init {
        loadData()
    }

    private suspend fun getRelationTags() {
        return
    }

    fun loadData(
        pageNum: Int = tagList.pageNum
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            tagList.loading.value = true
            val res = BiliApiService.userRelationApi
                .tags()
                .awaitCall()
                .gson<ResultInfo<List<TagInfo>>>()
            if (res.isSuccess) {
                tagList.pageNum = pageNum
                tagList.data.value = res.data
            } else {
                tagList.fail.value = res.message
            }

            val mid = userStore.state.info?.mid ?: return@launch
//
//            val res = BiliApiService.userApi
//                .followings(
//                    mid = mid.toString(),
//                    pageNum = pageNum,
//                    pageSize = list.pageSize,
//                    order = orderType.value
//                )
//                .awaitCall()
//                .gson<ResultInfo<FollowingsInfo>>()
//            if (res.isSuccess) {
//                list.pageNum = pageNum
//                list.finished.value = res.data == null || res.data.list.isEmpty()
//                if (res.data != null) {
//                    if (pageNum == 1) {
//                        list.data.value = res.data.list
//                    } else {
//                        list.data.value = mutableListOf<FollowingItemInfo>().apply {
//                            addAll(list.data.value)
//                            addAll(res.data.list)
//                        }
//                    }
//                    list.finished.value = res.data.list.size < list.pageSize
//                }
//            } else {
//                list.fail.value = res.message
//            }
        } catch (e: Exception) {
            tagList.fail.value = "无法连接到御坂网络"
        } finally {
            tagList.loading.value = false
            isRefreshing.value = false
        }
    }

//    fun loadMore() {
//        if (!list.finished.value && !list.loading.value) {
//            loadData(list.pageNum + 1)
//        }
//    }

    fun refresh() {
        isRefreshing.value = true
        tagList.finished.value = false
        tagList.fail.value = ""
        loadData(1)
    }

//    fun attention(
//        index: Int,
//    ) = viewModelScope.launch(Dispatchers.IO) {
//        try {
//            if (!userStore.isLogin()) {
//                withContext(Dispatchers.Main) {
//                    PopTip.show("请先登录")
//                }
//                return@launch
//            }
//            val item = list.data.value[index]
//            val mode = if (item.isFollowing) {
//                2
//            } else {
//                1
//            }
//            val newAttribute = if (item.isFollowing) {
//                0
//            } else {
//                2
//            }
//            val res = BiliApiService.userApi
//                .attention(item.mid, mode)
//                .awaitCall().gson<MessageInfo>()
//            if (res.code == 0) {
//                list.data.value = list.data.value.map {
//                    if (item.mid == it.mid) {
//                        it.copy(attribute = newAttribute)
//                    } else {
//                        it
//                    }
//                }
//                withContext(Dispatchers.Main) {
//                    PopTip.show(
//                        if (mode == 2) {
//                            "已取消关注"
//                        } else {
//                            "关注成功"
//                        }
//                    )
//                }
//            } else {
//                withContext(Dispatchers.Main) {
//                    PopTip.show(res.message)
//                }
//            }
//        } catch (e: Exception) {
//            withContext(Dispatchers.Main) {
//                PopTip.show("网络错误")
//            }
//            e.printStackTrace()
//        }
//    }

    fun toSearchPage() {
        fragment.findComposeNavController()
            .navigate(SearchFollowPage())
    }

    fun showOrderPopupMenu(view: View) {
        val pm = UserFollowOrderPopupMenu(
            activity,
            view,
            checkedValue = orderType.value
        )
        pm.setOnMenuItemClickListener {
            it.isChecked = true
            val value = arrayOf("attention", "")[it.itemId]
            if (value != orderType.value) {
                orderType.value = value
            }
            false
        }
        pm.show()
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun MyFollowPageContent(
    viewModel: MyFollowPageViewModel,
) {
    val userStore: UserStore by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(LocalView.current)

    val tagList by viewModel.tagList.data.collectAsState()
    val tagListLoading by viewModel.tagList.loading.collectAsState()
    val tagListFail by viewModel.tagList.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val orderType by viewModel.orderType.collectAsState()
    val isLogin = userStore.isLogin()

    val scope = rememberCoroutineScope()

    PageConfig(
        title = "我的关注",
        menus = listOf(
            myMenuItem {
                key = MenuKeys.search
                iconFileName = "ic_search_gray"
                title = "搜索"
            },
            myMenuItem {
                key = MenuKeys.filter
                iconFileName = "ic_baseline_filter_list_grey_24"
                title = viewModel.orderTypeToNameMap[orderType]
            },
        )
    )
    PageMenuItemClick(viewModel) { view, item ->
        when (item.key) {
            MenuKeys.filter -> {
                viewModel.showOrderPopupMenu(view)
            }
            MenuKeys.search -> {
                viewModel.toSearchPage()
            }
        }
    }

    val pagerState = rememberPagerState(pageCount = { tagList.size })

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        PrimaryTabRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    top = windowInsets.topDp.dp,
                    start = windowInsets.leftDp.dp,
                    end = windowInsets.rightDp.dp,
                ),
            selectedTabIndex = pagerState.currentPage,
        ) {
            tagList.forEachIndexed { index, tab ->
                Tab(
                    text = {
                        Row() {
                            Text(
                                text = "${tab.name}(${tab.count})",
                                color = if (index == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                }
                            )
                        }
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
}