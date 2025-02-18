package cn.a10miaomiao.bilimiao.compose.pages.community

import android.content.Context
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
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.kongzue.dialogx.dialogs.PopTip
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
    private val userStore: UserStore by instance()

    var sortOrder = 3

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing
    val list = FlowPaginationInfo<ReplyInfo>()
    var upMid = -1L
    private var _cursor: CursorReply? = null

    private val _currentReply = MutableStateFlow<ReplyInfo?>(null)
    val currentReply: StateFlow<ReplyInfo?> get() = _currentReply

    init {
        loadData()
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
                    mode = bilibili.main.community.reply.v1.Mode.fromValue(sortOrder),
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
                upMid = it.upMid
            }
            listData.addAll(res.replies.filter { i1 ->
                listData.indexOfFirst { i2 -> i1.id == i2.id } == -1
            })
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

    fun switchLike(reply: ReplyInfo) {
        val index = list.data.value.indexOfFirst {
            it.id == reply.id
        }
        if (index != -1) {
            switchLike(index)
        }
    }

    fun switchLike(index: Int) = viewModelScope.launch(Dispatchers.IO) {
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

}