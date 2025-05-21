package cn.a10miaomiao.bilimiao.compose.pages.community

import android.content.Context
import android.view.View
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.main.community.reply.v1.CursorReply
import bilibili.main.community.reply.v1.CursorReq
import bilibili.main.community.reply.v1.MainListReq
import bilibili.main.community.reply.v1.ReplyGRPC
import bilibili.main.community.reply.v1.ReplyInfo
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState
import cn.a10miaomiao.bilimiao.compose.pages.community.components.ReplyEditDialogState
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentReplyInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
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
import org.kodein.di.instance

class MainReplyViewModel(
    override val di: DI,
    val oid: String,
    val type: Int,
    val extra: String = "",
    val filterTagName: String = "",
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
    private val _upMid = MutableStateFlow(-1L)
    val upMid: StateFlow<Long> get() = _upMid
    private var _cursor: CursorReply? = null

    private val _currentReply = MutableStateFlow<ReplyInfo?>(null)
    val currentReply: StateFlow<ReplyInfo?> get() = _currentReply

    init {
        loadData()
    }

    private fun addNewReply(reply: VideoCommentReplyInfo) {
        // TODO: 自定义通用Reply实体类
        _sortOrder.value = 2
        refreshList()
    }

    fun removeReplyItem(reply: ReplyInfo) {
        val newList = list.data.value.toMutableList()
        val index = newList.indexOfFirst {
            it.id == reply.id
        }
        if (index != -1) {
            newList.removeAt(index)
        }
        list.data.value = newList
    }

    private fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val req = MainListReq(
                oid = oid.toLong(),
                type = type.toLong(),
                rpid = 0,
                extra = extra,
                filterTagName = filterTagName,
                cursor = CursorReq(
                    mode = bilibili.main.community.reply.v1.Mode.fromValue(sortOrder.value),
                    next = _cursor?.next ?: 0,
                )
            )
            val res = BiliGRPCHttp.request {
                ReplyGRPC.mainList(req)
            }.awaitCall()
            val listData = list.data.value.toMutableList()
            if (_cursor == null) {
                res.upTop?.let {
                    listData.add(it)
                }
                res.adminTop?.let {
                    listData.add(it)
                }
                res.voteTop?.let {
                    listData.add(it)
                }
            }
            res.subjectControl?.let {
                _upMid.value = it.upMid
            }
            val replies = res.replies.filter { i1 ->
                listData.indexOfFirst { i2 -> i1.id == i2.id } == -1
            }
            listData.addAll(replies)
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

    fun likeReply(reply: ReplyInfo) {
        val index = list.data.value.indexOfFirst {
            it.id == reply.id
        }
        if (index != -1) {
            likeReplyAt(index)
        }
    }

    fun likeReplyAt(index: Int) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val item = list.data.value[index]
            val isLike = item.replyControl?.action == 1L
            val newAction = if (isLike) 0 else 1
            val res = BiliApiService.commentApi
                .action(1, item.oid.toString(), item.id.toString(), newAction)
                .awaitCall()
                .json<MessageInfo>()
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
                if (currentReply.value?.id == newItem.id) {
                    _currentReply.value = newItem
                }
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
                .json<MessageInfo>()
            if (res.isSuccess) {
                withContext(Dispatchers.Main) {
                    PopTip.show("删除成功")
                    messageDialog.close()
                    if (currentReply.value?.id == reply.id) {
                        _currentReply.value = null
                    }
                    removeReplyItem(reply)
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

    fun setCurrentReply(reply: ReplyInfo) {
        _currentReply.value = reply
    }

    fun clearCurrentReply() {
        _currentReply.value = null
    }

    fun isLogin() = userStore.isLogin()


    fun toUserPage(mid: String) {
        pageNavigation.navigate(UserSpacePage(
            id = mid,
        ))
    }

    fun openReplyDialog() {
        val params = ReplyEditParams(
            type = type,
            oid = oid,
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
        }
    }
}