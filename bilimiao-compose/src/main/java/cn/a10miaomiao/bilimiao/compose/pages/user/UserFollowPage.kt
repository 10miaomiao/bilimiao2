package cn.a10miaomiao.bilimiao.compose.pages.user

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.comm.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.comm.localFragmentNavController
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageMenuItemClick
import cn.a10miaomiao.bilimiao.compose.commponents.dialogs.SingleChoiceDialog
import cn.a10miaomiao.bilimiao.compose.commponents.dialogs.SingleChoiceItem
import cn.a10miaomiao.bilimiao.compose.commponents.dialogs.rememberDialogState
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.kongzue.dialogx.dialogs.PopTip
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

data class FollowingItemInfo(
    val mid: String,
    val attribute: Int, // 关注属性: 0：未关注, 2：已关注, 6：已互粉
    val mtime: Long,
    val special: Int, // 特别关注标志: 0：否, 1：是
    val uname: String,
    val face: String,
    val sign: String,
    val face_nft: Int,
    val nft_icon: String,
) {
    val isFollowing get() = attribute == 2 || attribute == 6
}

data class FollowingsInfo(
    val list: List<FollowingItemInfo>,
    val re_version: Int,
    val total: Int,
)

class UserFollowPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val activity by instance<Activity>()

    private val userStore by instance<UserStore>()

    var mid = ""
        set(value) {
            if (field != value) {
                field = value
                loadData(1)
            }
        }

    val count = MutableStateFlow(1)
    val isRefreshing = MutableStateFlow(false)
    val listState = MutableStateFlow(LazyListState(0, 0))
    val list = FlowPaginationInfo<FollowingItemInfo>()

    val orderTypeList = listOf(
        SingleChoiceItem("最常访问", "attention"),
        SingleChoiceItem("关注顺序", ""),
    )
    val orderType = MutableStateFlow("attention")

    fun add() {
        count.value = count.value + 1
    }

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
            val res = BiliApiService.userApi
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
        if (!list.finished.value && !list.loading.value && list.fail.value.isBlank()) {
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
            val res = BiliApiService.userApi
                .attention(item.mid, mode)
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
}

@Composable
fun UserFollowPage(
    mid: String,
    viewModel: UserFollowPageViewModel = diViewModel()
) {
    val userStore: UserStore by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(LocalView.current)
    val bottomAppBarHeight = windowStore.bottomAppBarHeightDp

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLogin = userStore.isLogin()

    val dialogState = rememberDialogState()
    val orderTypeList = viewModel.orderTypeList
    val orderType by viewModel.orderType.collectAsState()

    val nav = localFragmentNavController()

    val toUserDatailPage = remember(nav) {
        { id: String ->
            nav.navigate(
                "bilimiao://user/$id".toUri(),
                defaultNavOptions
            )
        }
    }

    LaunchedEffect(mid) {
        viewModel.mid = mid
    }

    PageConfig(
        title = if (userStore.isSelf(mid)) {
            "我的关注"
        } else {
            "Ta的关注"
        },
        menus = listOf(
            myMenuItem {
                key = 1
                iconFileName = "ic_baseline_filter_list_grey_24"
                title = "排序"
            }
        )
    )
    PageMenuItemClick(viewModel, dialogState) {
        when (it.key) {
            1 -> {
                dialogState.openDialog = true
            }
        }
    }
    SingleChoiceDialog(
        dialogState,
        title = "选择排序方式",
        list = orderTypeList,
        selected = orderType,
        onChange = viewModel::changeOrderType
    )
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = { viewModel.refresh() },
    ) {
        LazyColumn() {
            item(key = "top") {
                Spacer(modifier = Modifier.height(windowInsets.topDp.dp))
            }
            items(list.size, { list[it].mid }) {
                val item = list[it]
                Box(
                    modifier = Modifier.padding(5.dp),
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { toUserDatailPage(item.mid) }
                                .padding(10.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            GlideImage(
                                imageModel = UrlUtil.autoHttps(item.face),
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .padding(horizontal = 5.dp),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = item.uname,
                                    maxLines = 1,
                                    modifier = Modifier.padding(bottom = 5.dp),
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = item.sign,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.outline,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Button(
                                onClick = { viewModel.attention(it) },
                                modifier = Modifier
                                    .padding(0.dp)
                                    .size(65.dp, 30.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(5.dp),
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
            }

            item(key = "bottom") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (listFinished) {
                        Text(
                            "下面没有了",
                            modifier = Modifier.padding(start = 5.dp),
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 14.sp,
                        )
                    } else if (listFail.isNotBlank()) {
                        Text(
                            listFail,
                            modifier = Modifier.padding(start = 5.dp),
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 14.sp,
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp,
                        )
                        Text(
                            "加载中",
                            modifier = Modifier.padding(start = 5.dp),
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 14.sp,
                        )
                    }
                }
                LaunchedEffect(Unit) {
                    viewModel.loadMore()
                }
            }
            item {
                Spacer(modifier = Modifier.height(windowInsets.bottomDp.dp + bottomAppBarHeight.dp))
            }
        }
    }
}