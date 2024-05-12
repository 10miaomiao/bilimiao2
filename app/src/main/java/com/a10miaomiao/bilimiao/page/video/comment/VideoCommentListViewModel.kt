package com.a10miaomiao.bilimiao.page.video.comment

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.main.community.reply.v1.CursorReply
import bilibili.main.community.reply.v1.CursorReq
import bilibili.main.community.reply.v1.MainListReq
import bilibili.main.community.reply.v1.ReplyGRPC
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.commponents.comment.VideoCommentViewInfo
import com.a10miaomiao.bilimiao.page.video.comment.VideoCommentViewAdapter.convertToVideoCommentViewInfo
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class VideoCommentListViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val userStore: UserStore by instance()

    val id by lazy { fragment.requireArguments().getString(MainNavArgs.id, "") }
    val title by lazy { fragment.requireArguments().getString(MainNavArgs.title, "") }
    val cover by lazy { fragment.requireArguments().getString(MainNavArgs.cover, "") }
    val name by lazy { fragment.requireArguments().getString(MainNavArgs.name, "") }

    var sortOrder = 3

    var triggered = false
    var list = PaginationInfo<VideoCommentViewInfo>()
    var upMid = -1L
    private var _cursor: CursorReply? = null

    init {
        loadData()
    }

    private fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            ui.setState {
                list.loading = true
            }
            val req = MainListReq(
                oid = id.toLong(),
                type = 1,
                rpid = 0,
                cursor = CursorReq(
                    mode = bilibili.main.community.reply.v1.Mode.fromValue(sortOrder),
                    next = _cursor?.next ?: 0,
                )
            )
            val res = BiliGRPCHttp.request {
                ReplyGRPC.mainList(req)
            }.awaitCall()
            if (_cursor == null) {
                list.data = mutableListOf()
                res.upTop?.let {
                    list.data.add(
                        convertToVideoCommentViewInfo(it)
                    )
                }
                res.adminTop?.let {
                    list.data.add(
                        convertToVideoCommentViewInfo(it)
                    )
                }
                res.voteTop?.let {
                    list.data.add(
                        convertToVideoCommentViewInfo(it)
                    )
                }
            }
            ui.setState {
                res.subjectControl?.let {
                    upMid = it.upMid
                }
                list.data.addAll(res.replies.map(
                    VideoCommentViewAdapter::convertToVideoCommentViewInfo
                ))
                _cursor = res.cursor
                if (res.cursor?.isEnd == true) {
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

    fun loadMode() {
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
                PopTip.show("喵喵被搞坏了:" + e.message ?: e.toString())
            }
        }
    }

    fun isLogin() = userStore.isLogin()

}