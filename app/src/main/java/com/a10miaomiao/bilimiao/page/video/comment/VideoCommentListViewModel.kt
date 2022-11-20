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
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.video.SubmitVideosInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentReplyInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.network.request
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class VideoCommentListViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val userStore: UserStore by instance()

    val id by lazy { fragment.requireArguments().getString(MainNavGraph.args.id, "") }

    var sortOrder = 3

    var triggered = false
    var list = PaginationInfo<ReplyOuterClass.ReplyInfo>()
    private var _cursor: ReplyOuterClass.CursorReply? = null

    init {
        loadData()
    }

    private fun loadData() = viewModelScope.launch(Dispatchers.IO){
        try {
            ui.setState {
                list.loading = true
            }
            val req = ReplyOuterClass.MainListReq.newBuilder().apply {
                oid = id.toLong()
                type = 1
                rpid = 0
                cursor = ReplyOuterClass.CursorReq.newBuilder().apply {
                    modeValue = sortOrder
                    _cursor?.let {
                        next = it.next
//                        prev = it.prev
                    }
                }.build()
            }.build()
            val res = ReplyGrpc.getMainListMethod().request(req)
                .awaitCall()
            if (_cursor == null){
                list.data = mutableListOf()
            }
            ui.setState {
                list.data.addAll(res.repliesList)
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

    fun loadMode () {
        val (loading, finished) = this.list
        if (!finished && !loading) {
            loadData()
        }
    }

    fun refreshList() {
        ui.setState {
            list = PaginationInfo()
            _cursor = null
            triggered = true
            loadData()
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
                    .build()
                withContext(Dispatchers.Main) {
                    updateView(newItem)
                }
            } else {
                withContext(Dispatchers.Main) {
                    toast(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                toast("喵喵被搞坏了")
            }
        }
    }
    
}