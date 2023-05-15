package com.a10miaomiao.bilimiao.page.video.comment

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.main.community.reply.v1.ReplyGrpc
import bilibili.main.community.reply.v1.ReplyOuterClass
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.network.request
import com.a10miaomiao.bilimiao.comm.store.UserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class VideoCommentDetailViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val userStore: UserStore by instance()

    val id by lazy { fragment.requireArguments().getString(MainNavArgs.id, "") }
    var reply: VideoCommentDetailParam

    // 0：按时间，1：按点赞数，2：按回复数
    var sortOrder = 2

    var triggered = false
    var list = PaginationInfo<ReplyOuterClass.ReplyInfo>()
    private var _cursor: ReplyOuterClass.CursorReply? = null

    init {
        reply = fragment.requireArguments().getParcelable<VideoCommentDetailParam>("reply")!!
        loadData()
    }

    private fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            ui.setState {
                list.loading = true
            }
            val req = ReplyOuterClass.DetailListReq.newBuilder().apply {
                oid = reply.oid
                root = reply.rpid
                type = 1
                scene = ReplyOuterClass.DetailListScene.REPLY
                _cursor?.let {
                    cursor = ReplyOuterClass.CursorReq.newBuilder()
                        .setPrev(it.prev)
                        .setNext(it.next)
                        .setMode(it.mode)
                        .build()
                }
            }.build()
            val res = ReplyGrpc.getDetailListMethod().request(req)
                .awaitCall()
            if (_cursor == null){
                list.data = mutableListOf()
            }
            ui.setState {
                list.data.addAll(res.root.repliesList)
                _cursor = res.cursor
                if (res.cursor.isEnd) {
                    list.finished = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ui.setState {
                list.fail = true
            }
        } finally {
            ui.setState {
                list.loading = false
                triggered = false
            }
        }
    }

    private fun _loadData() {
        loadData()
    }

    fun loadMode () {
        val (loading, finished, pageNum) = this.list
        if (!finished && !loading) {
            loadData(pageNum = pageNum + 1)
        }
    }

    fun refreshList() {
        ui.setState {
            list = PaginationInfo()
            triggered = true
            loadData()
            _cursor = null
        }
    }

    fun setRootLike() = viewModelScope.launch(Dispatchers.IO){
        try {
            val newAction = if (reply.action == 1L) {
                0
            } else { 1 }
            val res = BiliApiService.commentApi
                .action(1, reply.oid.toString(), reply.rpid.toString(), newAction)
                .awaitCall()
                .gson<MessageInfo>()
            if (res.isSuccess) {
                ui.setState {
                    reply = reply.copy(
                        action = newAction.toLong(),
                        like = reply.like - 1 + newAction * 2
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    context.toast(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                context.toast("喵喵被搞坏了:" + e.message ?: e.toString())
            }
        }
    }

    fun setLike(
        index: Int,
        updateView: (item: ReplyOuterClass.ReplyInfo) -> Unit,
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            val item = list.data[index]
            val newAction = if (item.replyControl.action == 1L) {
                0
            } else { 1 }
            val res = BiliApiService.commentApi
                .action(1, item.oid.toString(), item.id.toString(), newAction)
                .awaitCall()
                .gson<MessageInfo>()
            if (res.isSuccess) {
                val replyControl = item.replyControl.toBuilder()
                    .setAction(newAction.toLong())
                    .build()
                val newItem = item.toBuilder()
                    .setReplyControl(replyControl)
                    .setLike(item.like - 1 + newAction * 2)
                    .build()
                withContext(Dispatchers.Main) {
                    updateView(newItem)
                }
            } else {
                withContext(Dispatchers.Main) {
                    context.toast(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                context.toast("喵喵被搞坏了:" + e.message ?: e.toString())
            }
        }
    }



}