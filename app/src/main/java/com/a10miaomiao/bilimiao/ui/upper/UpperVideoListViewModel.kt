package com.a10miaomiao.bilimiao.ui.upper

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.AndroidRuntimeException
import com.a10miaomiao.bilimiao.entity.*
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class UpperVideoListViewModel(val owner: Owner) : ViewModel() {

    val list = MiaoList<SubmitVideosInfo>()
    val loading = MutableLiveData<Boolean>()
    val loadState = MutableLiveData<LoadMoreView.State>()
    var pageNum = 1
    val pageSize = 10

    init {
        loadData()
        loading.value = true
        loadState.value = LoadMoreView.State.LOADING
    }

    fun loadData() {
        loading.value = true
        val url = BiliApiService.getUpperVideo(owner.mid, pageNum, pageSize)
        DebugMiao.log(url)
        MiaoHttp.getJson<ResultInfo<SubmitVideos>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ r ->
                    loading.value = false
                    val archive = r.data.list.vlist
                    list.addAll(archive)
                    if (archive.size < pageSize) {
                        loadState.value = LoadMoreView.State.NOMORE
                    }
                }, { e ->
                    loading.value = false
                    loadState.value = LoadMoreView.State.FAIL
                })
    }

    fun refreshList() {
        pageNum = 1
        list.clear()
        loadState.value = LoadMoreView.State.LOADING
        loadData()
    }

}