package cn.a10miaomiao.bilimiao.compose.pages.community.content

import android.view.View
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import bilibili.main.community.reply.v1.CursorReply
import bilibili.main.community.reply.v1.CursorReq
import bilibili.main.community.reply.v1.DetailListReq
import bilibili.main.community.reply.v1.DetailListScene
import bilibili.main.community.reply.v1.MainListReq
import bilibili.main.community.reply.v1.ReplyGRPC
import bilibili.main.community.reply.v1.ReplyInfo
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.community.ReplyItemBox
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoTitleBar
import cn.a10miaomiao.bilimiao.compose.pages.community.ReplyEditParams
import cn.a10miaomiao.bilimiao.compose.pages.community.components.ReplyEditDialog
import cn.a10miaomiao.bilimiao.compose.pages.community.components.ReplyEditDialogState
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage
import cn.a10miaomiao.bilimiao.compose.pages.user.components.TitleBar
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentReplyInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

private class ReplyDetailContentViewModel(
    override val di: DI,
    private val currentReply: ReplyInfo,
    private val onDeletedReply: (ReplyInfo) -> Unit = {},
) : ViewModel(), DIAware {

    private val pageNavigation: PageNavigation by instance()
    private val messageDialog: MessageDialogState by instance()

    private val userStore: UserStore by instance()

    val editDialogState = ReplyEditDialogState(
        scope = viewModelScope,
        onAddReply = ::addNewReply,
    )

    private var _sortOrder = MutableStateFlow(3)
    val sortOrder: StateFlow<Int> get() = _sortOrder
    val sortOrderList = listOf(
        2 to "按时间",
        3 to "按热度",
    )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing
    val list = FlowPaginationInfo<ReplyInfo>()
    var upMid = -1L
    private var _cursor: CursorReply? = null

    init {
        loadData()
    }

    private fun addNewReply(reply: VideoCommentReplyInfo) {
        // TODO: 自定义通用Reply实体类
        refreshList()
    }

    private fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val req = DetailListReq(
                oid = currentReply.oid,
                type = currentReply.type,
                root = currentReply.id,
                scene = DetailListScene.REPLY,
                cursor = CursorReq(
                    mode = bilibili.main.community.reply.v1.Mode.fromValue(sortOrder.value),
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

    fun likeReplyAt(index: Int) = viewModelScope.launch(Dispatchers.IO) {
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

    fun deleteReply(
        reply: ReplyInfo
    ) {
        messageDialog.open(
            title = "提示",
            text = "确定要删除这条评论：${reply.content?.message}",
            confirmButton = {
                TextButton(
                    onClick = {
                        messageDialog.close()
                        requestDeleteReply(reply)
                    },
                ) {
                    Text("确定")
                }
            },
            closeText = "取消",
            showClose = true,
        )
    }

    fun requestDeleteReply(
        reply: ReplyInfo
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            withContext(Dispatchers.Main) {
                messageDialog.loading("正在删除")
            }
            val res = BiliApiService.commentApi
                .del(
                    type = reply.type.toInt(),
                    oid = reply.oid.toString(),
                    rpid = reply.id.toString(),
                )
                .awaitCall()
                .gson<MessageInfo>()
            if (res.isSuccess) {
                withContext(Dispatchers.Main) {
                    PopTip.show("删除成功")
                    messageDialog.close()
                    if (currentReply.id == reply.id) {
                        onDeletedReply(reply)
                    } else {
                        val newList = list.data.value.toMutableList()
                        val index = newList.indexOfFirst {
                            it.id == reply.id
                        }
                        if (index != -1) {
                            newList.removeAt(index)
                        }
                        list.data.value = newList
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    TipDialog.show(res.message, WaitDialog.TYPE.WARNING)
                    messageDialog.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                messageDialog.alert(
                    title = "喵喵被搞坏了",
                    text = e.message ?: e.toString()
                )
            }
        }
    }

    fun setSortOrder(value: Int) {
        _sortOrder.value = value
        refreshList(false)
    }

    fun isLogin() = userStore.isLogin()

    fun toUserPage(mid: String) {
        pageNavigation.navigate(UserSpacePage(
            id = mid,
        ))
    }

    fun openReplyDialog(reply: ReplyInfo) {
        val params = ReplyEditParams(
            type = 3,
            oid = reply.oid.toString(),
            root = currentReply.id.toString(),
            parent = reply.id.toString(),
            name = reply.member?.name ?: "",
        )
        editDialogState.show(params)
    }

    fun openReplyDialog() {
        val params = ReplyEditParams(
            type = currentReply.type.toInt(),
            oid = currentReply.oid.toString(),
            root = currentReply.id.toString(),
            parent = currentReply.id.toString(),
            name = currentReply.member?.name ?: "",
        )
        editDialogState.show(params)
    }

    fun menuItemClick(view: View, item: MenuItemPropInfo) {
        when (val key = item.key) {
            in 0..10 -> {
                setSortOrder(key!!)
            }
            MenuKeys.send -> {
                openReplyDialog()
            }
            MenuKeys.delete -> {
                deleteReply(currentReply)
            }
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ReplyDetailContent(
    reply: ReplyInfo,
    innerPadding: PaddingValues,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    usePageConfig: Boolean = false,
    onCloseClick: () -> Unit = {},
    onDeletedReply: (ReplyInfo) -> Unit = {},
    onLikeReply: (ReplyInfo) -> Unit = {},
) {
    val userStore by rememberInstance<UserStore>()
    val userState by userStore.stateFlow.collectAsState()
    val viewModel = diViewModel(
        key = "reply-detail-${reply.id}"
    ) {
        ReplyDetailContentViewModel(
            di = it,
            currentReply = reply,
            onDeletedReply = onDeletedReply,
        )
    }

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val upMid = viewModel.upMid

    if (usePageConfig) {
        val memberName = reply.member?.name
        val memberId = reply.member?.mid
        val configId = PageConfig(
            title = if (memberName.isNullOrBlank())
                "评论详情"
            else
                "${memberName}\n的\n评论",
            menu = rememberMyMenu {
                myItem {
                    key = MenuKeys.send
                    iconFileName = "ic_baseline_send_24"
                    title = "回复评论"
                }
                if (userStore.isSelf(memberId?.toString() ?: "")) {
                    myItem {
                        key = MenuKeys.delete
                        iconFileName = "ic_baseline_delete_outline_24"
                        title = "删除评论"
                    }
                }
//                if (viewModel.enterPageUrl != null) {
//                    myItem {
//                        key = MenuKeys.url
//                        iconFileName = "ic_link_black_24dp"
//                        title = "评论来源"
//                    }
//                }
            }
        )
        PageListener(
            configId = configId,
            onMenuItemClick = viewModel::menuItemClick
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MiaoTitleBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
                ),
            icon = {
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
            },
            title = {
                Text(
                    text = "评论详情",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            },
            action = {
                if (!usePageConfig) {
                    IconButton(
                        onClick = viewModel::openReplyDialog
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Comment,
                            contentDescription = "回复评论",
                        )
                    }
                }
            }
        )
        SwipeToRefresh(
            refreshing = isRefreshing,
            onRefresh = { viewModel.refreshList() },
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                    ),
                contentPadding = PaddingValues(
                    bottom = innerPadding.calculateBottomPadding(),
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
                ),
            ) {
                item {
                    val replyMid = reply.mid
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
                        isUpper = replyMid == upMid,
//                        showDelete = userState.isSelf(replyMid),
                        onAvatarClick = {
                            viewModel.toUserPage(reply.mid.toString())
                        },
                        onLikeClick = {
                            onLikeReply(reply)
                        },
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
                    val replyMid = replyItem.mid
                    ReplyItemBox(
                        modifier = Modifier.fillMaxWidth(),
                        item = replyItem,
                        isUpper = replyMid == upMid,
                        showDelete = userState.isSelf(replyMid),
                        onAvatarClick = {
                            viewModel.toUserPage(replyItem.mid.toString())
                        },
                        onLikeClick = {
                            viewModel.likeReplyAt(it)
                        },
                        onReplyClick = {
                            viewModel.openReplyDialog(replyItem)
                        },
                        onDeleteClick = {
                            viewModel.deleteReply(replyItem)
                        }
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
    }


    ReplyEditDialog(
        state = viewModel.editDialogState
    )

}