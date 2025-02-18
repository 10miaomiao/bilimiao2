package cn.a10miaomiao.bilimiao.compose.pages.community.content

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.main.community.reply.v1.CursorReply
import bilibili.main.community.reply.v1.CursorReq
import bilibili.main.community.reply.v1.DetailListReq
import bilibili.main.community.reply.v1.DetailListScene
import bilibili.main.community.reply.v1.MainListReq
import bilibili.main.community.reply.v1.ReplyGRPC
import bilibili.main.community.reply.v1.ReplyInfo
import cn.a10miaomiao.bilimiao.compose.assets.BilimiaoIcons
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.Common
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Menufold
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Menuunfold
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.community.ReplyItemBox
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

private class ReplyDetailContentViewModel(
    override val di: DI,
    private val oid: Long = 0L,
    private val type: Long = 0L,
    private val root: Long = 0L,
) : ViewModel(), DIAware {

    private val pageNavigation: PageNavigation by instance()

    private val userStore: UserStore by instance()

    var sortOrder = 3

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing
    val list = FlowPaginationInfo<ReplyInfo>()
    var upMid = -1L
    private var _cursor: CursorReply? = null

    init {
        loadData()
    }

    private fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val req = DetailListReq(
                oid = oid,
                type = type,
                root = root,
                scene = DetailListScene.REPLY,
                cursor = CursorReq(
                    mode = bilibili.main.community.reply.v1.Mode.fromValue(sortOrder),
                    next = _cursor?.next ?: 0,
                )
            )
            val res = BiliGRPCHttp.request {
                ReplyGRPC.detailList(req)
            }.awaitCall()
            val listData = list.data.value.toMutableList()
            res.subjectControl?.let {
                upMid = it.upMid
            }
            res.root?.let {
                listData.addAll(it.replies.filter { i1 ->
                    listData.indexOfFirst { i2 -> i1.id == i2.id } == -1
                })
            }
            list.data.value = listData
            _cursor = res.cursor
            if (res.cursor?.isEnd == true) {
                list.finished.value = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = e.message ?: e.toString()
        } finally {
            list.loading.value = false
            _isRefreshing.value = false
        }
    }

    fun loadMore() {
        if (!this.list.finished.value && !this.list.loading.value) {
            loadData()
        }
    }

    fun refreshList(
        refreshing: Boolean = true,
    ) {
        list.reset()
        _cursor = null
        _isRefreshing.value = refreshing
        loadData()
    }

    fun switchLike(index: Int) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val item = list.data.value[index]
            val isLike = item.replyControl?.action == 1L
            val newAction = if (isLike) 0 else 1
            val res = BiliApiService.commentApi
                .action(1, item.oid.toString(), item.id.toString(), newAction)
                .awaitCall()
                .gson<MessageInfo>()
            if (res.isSuccess) {
                val likeNum = if (isLike) item.like - 1 else item.like + 1
                val newItem = item.copy(
                    replyControl = item.replyControl?.copy(
                        action = newAction.toLong(),
                    ),
                    like = likeNum,
                )
                val newList = list.data.value.toMutableList()
                newList[index] = newItem
                list.data.value = newList
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show("喵喵被搞坏了:" + e.message ?: e.toString())
        }
    }

    fun isLogin() = userStore.isLogin()

    fun toUserPage(mid: String) {
        pageNavigation.navigate(UserSpacePage(
            id = mid,
        ))
    }

}


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun ReplyDetailContent(
    reply: ReplyInfo,
    innerPadding: PaddingValues,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onCloseClick: () -> Unit = {},
    onLikeRootClick: () -> Unit = {},
) {
    val viewModel = diViewModel(
        key = "reply-detail-${reply.id}"
    ) {
        ReplyDetailContentViewModel(
            di = it,
            oid = reply.oid,
            type = reply.type,
            root = reply.id,
        )
    }

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val upMid = viewModel.upMid


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface,
            ),
        contentPadding = innerPadding,
    ) {
        stickyHeader {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentColor = MaterialTheme.colorScheme.onBackground,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box {
                            IconButton(
                                onClick = onCloseClick
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(24.dp),
                                )
                            }
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "评论详情",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                    )
                }
            }
        }
        item {
            ReplyItemBox(
                modifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            rememberSharedContentState("reply-${reply.id}"),
                            animatedVisibilityScope,
                        )
                    }
                } else {
                    Modifier
                }.fillMaxWidth(),
                item = reply,
                upMid = upMid,
                onAvatarClick = {
                    viewModel.toUserPage(reply.mid.toString())
                },
                onLikeClick = onLikeRootClick,
            )
        }
        item {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${reply.count}条回复",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    )
                )
                HorizontalDivider(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                )
            }

        }
        items(
            list.size,
            { list[it].id }
        ) {
            val replyItem = list[it]
            ReplyItemBox(
                modifier = Modifier.fillMaxWidth(),
                item = replyItem,
                upMid = upMid,
                onAvatarClick = {
                    viewModel.toUserPage(replyItem.mid.toString())
                },
                onLikeClick = {
                    viewModel.switchLike(it)
                },
            )
        }
        item() {
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