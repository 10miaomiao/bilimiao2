package cn.a10miaomiao.bilimiao.compose.pages.user

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.components.user.UserInfoCard
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.UserStore
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
class UserFollowPage(
    private val id: String
) : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel = diViewModel<UserFollowPageViewModel>()
        val mid = id
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
    private val pageNavigation by instance<PageNavigation>()
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

    val orderType = MutableStateFlow("attention")
    val orderTypeToNameMap = mapOf(
        "attention" to "最常访问",
        "" to "关注顺序",
    )

    fun changeOrderType(value: String) {
        if (orderType.value != value) {
            orderType.value = value
            list.data.value = emptyList()
            list.finished.value = false
            list.fail.value = ""
            loadData(1)
        }
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
                .json<ResponseData<FollowingsInfo>>()
            if (res.isSuccess) {
                list.pageNum = pageNum
                val result = res.requireData()
                list.finished.value = res.data == null || result.list.isEmpty()
                if (res.data != null) {
                    if (pageNum == 1) {
                        list.data.value = result.list
                    } else {
                        list.data.value = mutableListOf<FollowingItemInfo>().apply {
                            addAll(list.data.value)
                            addAll(result.list)
                        }
                    }
                    list.finished.value = result.list.size < list.pageSize
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
                .awaitCall().json<MessageInfo>()
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
        pageNavigation.navigate(UserSpacePage(id))
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

    val orderTypeToNameMap = viewModel.orderTypeToNameMap
    val orderType by viewModel.orderType.collectAsState()

    val pageConfigId = PageConfig(
        title = if (userStore.isSelf(viewModel.mid)) {
            "我的关注"
        } else {
            "Ta的关注"
        },
        menu = remember(orderType) {
            myMenu {
                myItem {
                    key = 1
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
        pageConfigId,
        onMenuItemClick = fun(_, item) {
            val key = item.key ?: return
            if (item.key in orderTypeToNameMap.keys.indices) {
                val value = orderTypeToNameMap.keys.elementAt(key)
                viewModel.changeOrderType(value)
            }
        }
    )

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


    }
}