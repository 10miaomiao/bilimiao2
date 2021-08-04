package com.a10miaomiao.bilimiao.ui.user

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.entity.RegionTypeDetailsInfo
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.entity.VideoInfo
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.bilimiao.utils.getViewModel
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.toast
import java.lang.Exception

class HistoryViewModel(
        val context: Context
) : ViewModel() {

    private var loadDataDisposable: Disposable? = null

    val list = MiaoList<VideoInfo>()
    val loading = MiaoLiveData(false)
    val loadState = MiaoLiveData(LoadMoreView.State.LOADING)
    var pageNum = 1
    val pageSize = 20

    init {
        loadData()
    }

    fun loadData() {
        if (list.size >= pageNum * pageSize)
            return
        if (loadState == LoadMoreView.State.NOMORE || -loading) {
            return
        }
        loading set true
        val url = BiliApiService.getHistory(pageNum, pageSize)
        loadDataDisposable = MiaoHttp.getJson<ResultInfo<List<VideoInfo>>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter {
                    if (it.code != 0){
                        throw Exception(it.message)
                    }
                    true
                }
                .map { data -> data.data }
                .doOnNext { result ->
                    if (result.size < pageSize) {
                        loadState set LoadMoreView.State.NOMORE
                    }
                }
                .subscribe({ result ->
                    list.addAll(result)
                }, { err ->
                    loadState set LoadMoreView.State.FAIL
                    context.toast(err.message ?: "网络错误")
                    err.printStackTrace()
                }, {
                    loading set false
                })

    }

    fun refreshList() {
        pageNum = 1
        list.clear()
        loadState set LoadMoreView.State.LOADING
        loadData()
    }

    override fun onCleared(){
        loadDataDisposable?.dispose()
    }

}