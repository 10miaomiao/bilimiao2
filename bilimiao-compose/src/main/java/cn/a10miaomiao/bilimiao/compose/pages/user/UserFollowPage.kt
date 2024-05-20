package cn.a10miaomiao.bilimiao.compose.pages.user

import android.app.Activity
import android.view.View
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.base.stringPageArg
import cn.a10miaomiao.bilimiao.compose.comm.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageMenuItemClick
import cn.a10miaomiao.bilimiao.compose.commponents.dialogs.SingleChoiceItem
import cn.a10miaomiao.bilimiao.compose.commponents.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.commponents.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.pages.user.commponents.UserInfoCard
import cn.a10miaomiao.bilimiao.compose.pages.user.poup_menu.UserFollowOrderPopupMenu
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.navigation.pointerOrSelf
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
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


class UserFollowPage() : ComposePage() {

    val id = stringPageArg("id")

    override val route: String
        get() = "user/${id}/follow"

    @Composable
    override fun AnimatedContentScope.Content(navEntry: NavBackStackEntry) {
        val viewModel = diViewModel<UserFollowPageViewModel>()
        val mid = navEntry.arguments?.get(id) ?: ""
        LaunchedEffect(mid) {
            viewModel.mid = mid
        }
        UserFollowPageContent(viewModel = viewModel)
    }
}

private class UserFollowPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val activity by instance<Activity>()
    private val userStore by instance<UserStore>()

    var mid = "0"
        set(value) {
            if (field != value) {
                field = value
                loadData(1)
            }
        }

    val isRefreshing = MutableStateFlow(false)
    val listState = MutableStateFlow(LazyListState(0, 0))
    val list = FlowPaginationInfo<FollowingItemInfo>()

    val orderTypeList = listOf(
        SingleChoiceItem("最常访问", "attention"),
        SingleChoiceItem("关注顺序", ""),
    )
    val orderType = MutableStateFlow("attention")
    val orderTypeToNameMap = mapOf(
        "attention" to "最常访问",
        "" to "关注顺序",
    )

    fun changeOrderType(value: String) {
        orderType.value = value
        list.data.value = emptyList()
        list.finished.value = false
        list.fail.value = ""
        loadData(1)
    }

    fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val res = BiliApiService.userRelationApi
                .followings(
                    mid = mid,
                    pageNum = pageNum,
                    pageSize = list.pageSize,
                    order = orderType.value
                )
                .awaitCall()
                .gson<ResultInfo<FollowingsInfo>>()
            if (res.isSuccess) {
                list.pageNum = pageNum
                list.finished.value = res.data == null || res.data.list.isEmpty()
                if (res.data != null) {
                    if (pageNum == 1) {
                        list.data.value = res.data.list
                    } else {
                        list.data.value = mutableListOf<FollowingItemInfo>().apply {
                            addAll(list.data.value)
                            addAll(res.data.list)
                        }
                    }
                    list.finished.value = res.data.list.size < list.pageSize
                }
            } else {
                list.fail.value = res.message
            }
        } catch (e: Exception) {
            list.fail.value = "无法连接到御坂网络"
        } finally {
            list.loading.value = false
            isRefreshing.value = false
        }
    }

    fun loadMore() {
        if (!list.finished.value && !list.loading.value) {
            loadData(list.pageNum + 1)
        }
    }

    fun refresh() {
        isRefreshing.value = true
        list.finished.value = false
        list.fail.value = ""
        loadData(1)
    }

    fun attention(
        index: Int,
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            if (!userStore.isLogin()) {
                withContext(Dispatchers.Main) {
                    PopTip.show("请先登录")
                }
                return@launch
            }
            val item = list.data.value[index]
            val mode = if (item.isFollowing) {
                2
            } else {
                1
            }
            val newAttribute = if (item.isFollowing) {
                0
            } else {
                2
            }
            val res = BiliApiService.userRelationApi
                .modify(item.mid, mode)
                .awaitCall().gson<MessageInfo>()
            if (res.code == 0) {
                list.data.value = list.data.value.map {
                    if (item.mid == it.mid) {
                        it.copy(attribute = newAttribute)
                    } else {
                        it
                    }
                }
                withContext(Dispatchers.Main) {
                    PopTip.show(
                        if (mode == 2) {
                            "已取消关注"
                        } else {
                            "关注成功"
                        }
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show(res.message)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                PopTip.show("网络错误")
            }
            e.printStackTrace()
        }
    }

    fun toUserDetailPage(id: String) {
        val nav = fragment.findNavController().pointerOrSelf()
        nav.navigate(
            "bilimiao://user/$id".toUri(),
            defaultNavOptions
        )
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
                changeOrderType(value)
            }
            false
        }
        pm.show()
    }
}

@Composable
private fun UserFollowPageContent(
    viewModel: UserFollowPageViewModel,
) {
    val userStore: UserStore by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(LocalView.current)

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLogin = userStore.isLogin()

    val orderTypeList = viewModel.orderTypeList
    val orderType by viewModel.orderType.collectAsState()

    PageConfig(
        title = if (userStore.isSelf(viewModel.mid)) {
            "我的关注"
        } else {
            "Ta的关注"
        },
        menus = listOf(
            myMenuItem {
                key = 1
                iconFileName = "ic_baseline_filter_list_grey_24"
                title = viewModel.orderTypeToNameMap[orderType]
            }
        )
    )
    PageMenuItemClick(viewModel) { view, item ->
        when (item.key) {
            MenuKeys.filter -> {
                viewModel.showOrderPopupMenu(view)
            }
        }
    }

    SwipeToRefresh(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(400.dp),
            modifier = Modifier.padding(
                start = windowInsets.leftDp.dp,
                end = windowInsets.rightDp.dp,
            )
        ) {

            item(
                span = {
                    GridItemSpan(maxLineSpan)
                }
            ) {
                Spacer(modifier = Modifier.height(windowInsets.topDp.dp))
            }

            items(list.size, { list[it].mid }) {
                val item = list[it]
                Box(
                    modifier = Modifier.padding(5.dp),
                ) {
                    UserInfoCard(
                        name = item.uname,
                        face = item.face,
                        sign = item.sign,
                        onClick = {
                            viewModel.toUserDetailPage(item.mid)
                        }
                    ) {
                        Button(
                            onClick = { viewModel.attention(it) },
                            shape = MaterialTheme.shapes.small,
                            contentPadding = PaddingValues(
                                vertical = 4.dp,
                                horizontal = 12.dp,
                            ),
                            modifier = Modifier
                                .sizeIn(
                                    minWidth = 40.dp,
                                    minHeight = 30.dp
                                )
                                .padding(0.dp),
                            enabled = isLogin,
                            colors = if (item.isFollowing) {
                                ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray
                                )
                            } else {
                                ButtonDefaults.buttonColors()
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (!isLogin) {
                                    Text(
                                        text = "未登录",
                                        fontSize = 12.sp,
                                    )
                                } else if (item.isFollowing) {
                                    Text(
                                        text = "已关注",
                                        fontSize = 12.sp,
                                    )
                                } else {
                                    Icon(
                                        modifier = Modifier.size(15.dp),
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null,
                                    )
                                    Text(
                                        text = "关注",
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                        }
                    }
                }
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
//        LazyColumn(
//            modifier = Modifier.padding(
//                start = windowInsets.leftDp.dp,
//                end = windowInsets.rightDp.dp,
//            )
//        ) {
//            item(key = "top") {
//                Spacer(modifier = Modifier.height(windowInsets.topDp.dp))
//            }
//
//
//        }
    }
}