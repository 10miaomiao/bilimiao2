package com.a10miaomiao.bilimiao.ui.video

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.entity.comment.Page
import com.a10miaomiao.bilimiao.entity.comment.ReplyBean
import com.a10miaomiao.bilimiao.entity.comment.VideoComment
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.toast

class VideoCommentViewModel(
        val context: Context,
        val id: String
) : ViewModel() {

    private var minid = 0
    private var pageNum = 1
    private val pageSize = 20

    var list = MiaoList<ReplyBean>()
    var hotList = MiaoList<ReplyBean>()

    var loading = MiaoLiveData(false)
    val loadState = MiaoLiveData(LoadMoreView.State.LOADING)
    val pageInfo = MiaoLiveData(Page(
        0, 0, 0, 0
    ))

    var sortOrder = 1

    init {
        loadData()
    }

    fun loadData() {
        if (-loading)
            return
        if (-loadState == LoadMoreView.State.NOMORE)
            return
        loading set true
        val url = BiliApiService.getCommentList(id, sortOrder, pageNum, pageSize)
        MiaoHttp.getJson<ResultInfo<VideoComment>>(url)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ r ->
                    if (r.code == 0){
                        pageInfo set r.data.page
                        list.addAll(r.data.replies)
                        loading set false
                        pageNum++
                        if (r.data.page.count <= list.size){
                            loadState set LoadMoreView.State.NOMORE
                        }
                    }else{
                        context.toast(r.message)
                    }
                }, { e ->
                    e.printStackTrace()
                    loadState set LoadMoreView.State.FAIL
                    loading set false
                })
    }

    fun refreshList() {
        list.clear()
        hotList.clear()
        minid = 0
        pageNum = 1
        loadState set LoadMoreView.State.LOADING
        loadData()
    }

}