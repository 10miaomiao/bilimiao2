package com.a10miaomiao.bilimiao.ui.video

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.entity.VideoComment
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class VideoCommentViewModel(val id: String) : ViewModel() {

    private var minid = 0
    private val pageSize = 20

    var list = MiaoList<VideoComment.ReplyBean>()
    var hotList = MiaoList<VideoComment.ReplyBean>()

    var loading = MutableLiveData<Boolean>()
    val loadState = MutableLiveData<LoadMoreView.State>()

    init {
        loading.value = false
        loadState.value = LoadMoreView.State.LOADING
        loadData()
    }

    fun loadData() {
        if (loading.value!!)
            return
        if (loadState.value!! != LoadMoreView.State.LOADING)
            return
        loading.value = true
        val url = BiliApiService.getCommentList(id, minid, pageSize)
        MiaoHttp.getJson<ResultInfo<VideoComment>>(url)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ r ->
                    if (minid == 0) {
                        hotList.clear()
                        hotList.addAll(r.data.hots)
                    }
                    minid = r.data.cursor.min_id
                    list.addAll(r.data.replies)
                    loading.value = false
                    if (r.data.replies.size < pageSize){
                        loadState.value = LoadMoreView.State.NOMORE
                    }
                }, { e ->
                    loadState.value = LoadMoreView.State.FAIL
                    loading.value = false
                })
    }

    fun refreshList() {
        list.clear()
        hotList.clear()
        minid = 0
        loadState.value = LoadMoreView.State.LOADING
        loadData()
    }

}