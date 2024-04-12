package cn.a10miaomiao.bilimiao.compose.pages.user.content

import android.app.Activity
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.comm.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.commponents.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.commponents.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.pages.user.FollowingItemInfo
import cn.a10miaomiao.bilimiao.compose.pages.user.FollowingsInfo
import cn.a10miaomiao.bilimiao.compose.pages.user.InterrelationInfo
import cn.a10miaomiao.bilimiao.compose.pages.user.commponents.UserInfoCard
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore

import com.a10miaomiao.bilimiao.store.WindowStore
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.localDI
import org.kodein.di.compose.rememberDI
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

private class TagFollowContentModel(
    val tagId: Int,
    val orderType: String,
    override val di: DI,
) : ViewModel(), DIAware {

    private val activity by instance<Activity>()
    private val fragment by instance<Fragment>()
    private val userStore by instance<UserStore>()


//    var orderType = "attention"

    val count = MutableStateFlow(1)
    val isRefreshing = MutableStateFlow(false)
    val listState = MutableStateFlow(LazyListState(0, 0))
    val list = FlowPaginationInfo<FollowingItemInfo>()

    init {
        loadData()
    }

    fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val res = BiliApiService.userRelationApi
                .tagDetail(
                    tagId = tagId,
                    pageNum = pageNum,
                    pageSize = list.pageSize,
                    order = orderType
                )
                .awaitCall()
                .gson<ResultInfo<List<FollowingItemInfo>>>()
            if (res.isSuccess) {
                list.pageNum = pageNum
                val listData = getInterrelations(
                    res.data,
                    InterrelationInfo(
                        attribute = 2,
                        is_followed = true,
                    )
                )
                list.finished.value = listData.isEmpty()
                if (pageNum == 1) {
                    list.data.value = listData
                } else {
                    list.data.value = mutableListOf<FollowingItemInfo>().apply {
                        addAll(list.data.value)
                        addAll(listData)
                    }
                }
                list.finished.value = res.data.size < list.pageSize
            } else {
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

    suspend fun getInterrelations(
        list: List<FollowingItemInfo>,
        defaultInterrelation: InterrelationInfo,
    ): List<FollowingItemInfo> {
        val res = BiliApiService.userRelationApi
            .interrelations(list.map { it.mid })
            .awaitCall()
            .gson<ResultInfo<JsonObject>>(isDebug = true)
        val interrelationMap = if (res.isSuccess) {
            res.data
        } else {
            JsonObject()
        }
        return list.map {
            var interrelation = defaultInterrelation
            if (interrelationMap.has(it.mid)) {
                val interrelationJObj = interrelationMap[it.mid]
                if (interrelationJObj.isJsonObject) {
                    interrelation = Gson().fromJson(
                        interrelationJObj,
                        InterrelationInfo::class.java
                    )
                }
            }
            it.copy(
                attribute = interrelation.attribute,
                special = interrelation.special,
                mtime = interrelation.mtime,
                tag = interrelation.tag,
            )
        }
    }

    fun loadMore() {
        if (!list.finished.value && !list.loading.value) {
            loadData(list.pageNum + 1)
        }
    }

    fun refresh(
        refreshing: Boolean = true
    ) {
        isRefreshing.value = refreshing
        list.finished.value = false
        list.fail.value = ""
        loadData(1)
    }

    fun attention(
        index: Int,
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            if (!userStore.isLogin()) {
                PopTip.show("请先登录")
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
                PopTip.show(
                    if (mode == 2) {
                        "已取消关注"
                    } else {
                        "关注成功"
                    }
                )
            } else {
                 PopTip.show(res.message)
            }
        } catch (e: Exception) {
            PopTip.show("网络错误")
            e.printStackTrace()
        }
    }

    fun toUserDetailPage(id: String) {
        val nav = fragment.findNavController()
        nav.navigate(
            "bilimiao://user/$id".toUri(),
            defaultNavOptions
        )
    }
}

@Composable
internal fun TagFollowContent(
    tagId: Int,
    orderType: String,
) {
    val di = localDI()
    val viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = tagId.toString() + orderType,
        initializer = {
            TagFollowContentModel(
                tagId = tagId,
                orderType = orderType,
                di
            )
        }
    )

    val windowStore: WindowStore by rememberInstance()
    val userStore: UserStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLogin = userStore.isLogin()

//    LaunchedEffect(viewModel, orderType) {
//        if (orderType != viewModel.orderType) {
//            viewModel.orderType = orderType
//            viewModel.refresh(false)
//        }
//    }

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