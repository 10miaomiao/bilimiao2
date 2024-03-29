package com.a10miaomiao.bilimiao.page.video.comment

import android.content.Context
import android.os.Debug
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import bilibili.main.community.reply.v1.ReplyGrpc
import bilibili.main.community.reply.v1.ReplyOuterClass
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentReplyInfo
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.network.request
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.commponents.comment.VideoCommentViewContent
import com.a10miaomiao.bilimiao.commponents.comment.VideoCommentViewInfo
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
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

    val index by lazy { fragment.requireArguments().getInt(MainNavArgs.index, -1) }
    var reply: VideoCommentViewInfo? = null

    // 0：按时间，1：按点赞数，2：按回复数
    var sortOrder = 2

    var triggered = false
    var list = PaginationInfo<VideoCommentViewInfo>()
    var upMid = 0L
    private var _cursor: ReplyOuterClass.CursorReply? = null

    init {
        val arguments = fragment.requireArguments()
        if (arguments.containsKey(MainNavArgs.root)) {
            val rootId = arguments.getString(MainNavArgs.root, "0").toLong()
            getRootReplyInfo(rootId)
        } else if (arguments.containsKey(MainNavArgs.reply)) {
            reply = arguments.getParcelable(MainNavArgs.reply)
            loadData()
        }
    }

    private fun getRootReplyInfo(
        rootId: Long
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            ui.setState {
                list.loading = true
            }
            val req = ReplyOuterClass.ReplyInfoReq.newBuilder().apply {
                rpid = rootId
                scene = 1
            }.build()
            val res = ReplyGrpc.getReplyInfoMethod().request(req)
                .awaitCall()
            ui.setState {
                reply = VideoCommentViewAdapter.convertToVideoCommentViewInfo(res.reply)
                _cursor == null
                loadData()
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

    private fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val that = this@VideoCommentDetailViewModel
            val reply = that.reply ?: return@launch
            ui.setState {
                list.loading = true
            }
            val req = ReplyOuterClass.DetailListReq.newBuilder().apply {
                oid = reply.oid
                root = reply.id
                type = 1
                scene = ReplyOuterClass.DetailListScene.REPLY
                cursor = _cursor?.let {
                    ReplyOuterClass.CursorReq.newBuilder()
                        .setNext(it.next)
                        .setMode(it.mode)
                        .build()
                }
            }.build()
            val res = ReplyGrpc.getDetailListMethod().request(req)
                .awaitCall()

            if (_cursor == null) {
                list.data = mutableListOf()
            }
            ui.setState {
                upMid = res.subjectControl.upMid
                list.data.addAll(
                    res.root.repliesList.map(
                        VideoCommentViewAdapter::convertToVideoCommentViewInfo
                    )
                )
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

    fun loadMode() {
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

    fun setRootLike() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val that = this@VideoCommentDetailViewModel
            val reply = that.reply ?: return@launch
            val newAction = if (reply.isLike) {
                0
            } else {
                1
            }
            val res = BiliApiService.commentApi
                .action(1, reply.oid.toString(), reply.id.toString(), newAction)
                .awaitCall()
                .gson<MessageInfo>()
            if (res.isSuccess) {
                val newReply = reply.copy(
                    isLike = newAction == 1,
                    like = reply.like - 1 + newAction * 2
                )
                ui.setState {
                    that.reply = newReply
                }
                val navController = fragment.findNavController()
                navController.previousBackStackEntry?.savedStateHandle?.let {
                    it[MainNavArgs.index] = index
                    it[MainNavArgs.reply] = newReply
                }
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                PopTip.show(e.message ?: e.toString())
            }
        }
    }

    fun setLike(
        index: Int,
        updateView: (item: VideoCommentViewInfo) -> Unit,
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val item = list.data[index]
            val newAction = if (item.isLike) {
                0
            } else {
                1
            }
            val res = BiliApiService.commentApi
                .action(1, item.oid.toString(), item.id.toString(), newAction)
                .awaitCall()
                .gson<MessageInfo>()
            if (res.isSuccess) {
                val newItem = item.copy(
                    like = item.like - 1 + newAction * 2,
                    isLike = newAction == 1
                )
                withContext(Dispatchers.Main) {
                    updateView(newItem)
                }
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                PopTip.show(e.message ?: e.toString())
            }
        }
    }


    fun delete() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val that = this@VideoCommentDetailViewModel
            val reply = that.reply ?: return@launch
            withContext(Dispatchers.Main) {
                WaitDialog.show("正在删除")
            }
            val res = BiliApiService.commentApi
                .del(
                    reply.oid.toString(),
                    reply.id.toString()
                )
                .awaitCall()
                .gson<MessageInfo>()
            if (res.isSuccess) {
                withContext(Dispatchers.Main) {
                    TipDialog.show("已删除", WaitDialog.TYPE.SUCCESS)
                    // 回调到列表移除
                    val navController = fragment.findNavController()
                    navController.previousBackStackEntry?.savedStateHandle?.let {
                        it[MainNavArgs.index] = index
                        it[MainNavArgs.reply] = reply.copy(
                            isDelete = true,
                        )
                    }
                    navController.popBackStack()
                }
            } else {
                withContext(Dispatchers.Main) {
                    TipDialog.show(res.message, WaitDialog.TYPE.WARNING)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                TipDialog.show("删除失败", WaitDialog.TYPE.ERROR)
//                PopTip.show("喵喵被搞坏了:" + e.message ?: e.toString())
            }
        }
    }

    suspend fun delete(reply: VideoCommentViewInfo): Boolean {
        try {
            withContext(Dispatchers.Main) {
                WaitDialog.show("正在删除")
            }
            val res = BiliApiService.commentApi
                .del(
                    reply.oid.toString(),
                    reply.id.toString()
                )
                .awaitCall()
                .gson<MessageInfo>()
            if (res.isSuccess) {
                withContext(Dispatchers.Main) {
                    TipDialog.show("已删除", WaitDialog.TYPE.SUCCESS)
                }
                return true
            } else {
                withContext(Dispatchers.Main) {
                    TipDialog.show(res.message, WaitDialog.TYPE.WARNING)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                TipDialog.show("删除失败", WaitDialog.TYPE.ERROR)
//                PopTip.show("喵喵被搞坏了:" + e.message ?: e.toString())
            }
        }
        return false
    }

    fun isSelfReply(): Boolean {
        return reply?.let(::isSelfReply) ?: false
    }

    fun isSelfReply(info: VideoCommentViewInfo): Boolean {
        return userStore.isSelf(info.mid.toString())
    }

    fun isLogin() = userStore.isLogin()

}