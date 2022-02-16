package com.a10miaomiao.bilimiao.page.video.comment

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentReplyCursorInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentReplyInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.UserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    val id by lazy { fragment.requireArguments().getString(MainNavGraph.args.id, "") }
    val reply by lazy { fragment.requireArguments().getParcelable<VideoCommentReplyInfo>("reply")!! }

    private var maxid: String? = null
    var sortOrder = 2

    var triggered = false
    var list = PaginationInfo<VideoCommentReplyInfo>()

    init {
        loadData()
    }

    private fun loadData(
        minId: String? = null,
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            ui.setState {
                list.loading = true
            }
            val res = BiliApiService.videoAPI
                .commentReplyList(
                    oid = reply.oid,
                    rpid = reply.rpid_str,
                    minId = minId,
                    pageSize = list.pageSize,
                )
                .awaitCall()
                .gson<ResultInfo<VideoCommentReplyCursorInfo>>()
            DebugMiao.log(res)
            if (res.code == 0) {
                val result = res.data.root.replies
                if (result.size < list.pageSize) {
                    ui.setState { list.finished = true }
                }
                ui.setState {
                    if (minId == null) {
                        list.data = arrayListOf()
                    }
                    list.data.addAll(result)
                }
                maxid = res.data.cursor.max_id.toString()
            } else {
                context.toast(res.message)
                throw Exception(res.message)
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
        val (loading, finished) = this.list
        if (!finished && !loading) {
            loadData(maxid.toString())
        }
    }

    fun refreshList() {
        ui.setState {
            maxid = null
            list = PaginationInfo()
            triggered = true
            loadData()
        }
    }


}