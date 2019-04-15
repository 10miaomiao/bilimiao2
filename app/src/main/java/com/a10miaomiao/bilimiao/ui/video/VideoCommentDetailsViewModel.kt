package com.a10miaomiao.bilimiao.ui.video

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.entity.comment.ReplyBean
import com.a10miaomiao.bilimiao.entity.comment.ReplyCursor
import com.a10miaomiao.bilimiao.entity.comment.VideoComment
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class VideoCommentDetailsViewModel(var reply: ReplyBean) : ViewModel() {

    private var minid = 0
    private val pageSize = 20

    var list = MiaoList<ReplyBean>()

    var loading = MutableLiveData<Boolean>()
    val loadState = MutableLiveData<LoadMoreView.State>()

    init {
        loading.value = false
        loadState.value = LoadMoreView.State.LOADING
        if (reply.count >= reply.replies.size) {
            minid = reply.replies[reply.replies.size - 1].floor
            list.addAll(reply.replies)
            loadData()
        } else {
            loadState.value =  LoadMoreView.State.NOMORE
        }

    }


    fun loadData() {
        if (loading.value!!)
            return
        if (loadState.value!! != LoadMoreView.State.LOADING)
            return
        loading.value = true
        val url = BiliApiService.getCommentReplyList(reply.oid, reply.rpid_str, minid, pageSize)
        DebugMiao.log(url)
        MiaoHttp.getJson<ResultInfo<ReplyCursor>>(url)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ r ->
                    minid = r.data.cursor.min_id
                    list.addAll(r.data.root.replies)
                    loading.value = false
                    if (r.data.root.replies.size < pageSize) {
                        loadState.value = LoadMoreView.State.NOMORE
                    }
                }, { e ->
                    loadState.value = LoadMoreView.State.FAIL
                    loading.value = false
                })
    }

    fun refreshList() {
        list.clear()
        minid = 0
        loadState.value = LoadMoreView.State.LOADING
        loadData()
    }

}