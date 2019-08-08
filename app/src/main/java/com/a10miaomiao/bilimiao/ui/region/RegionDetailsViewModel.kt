package com.a10miaomiao.bilimiao.ui.region

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.a10miaomiao.bilimiao.entity.RegionTypeDetailsInfo
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.store.FilterStore
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.bilimiao.ui.commponents.model.DateModel
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.bilimiao.utils.RxBus
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import com.a10miaomiao.miaoandriod.adapter.MiaoRecyclerViewAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class RegionDetailsViewModel(
        val context: Context,
        val tid: Int
) : ViewModel() {

    var pageNum = 1
    val pageSize = 10
    var rankOrder = "click"  //排行依据
    var loading = MiaoLiveData(false)
    var loadState = MiaoLiveData(LoadMoreView.State.LOADING)
    var list = MiaoList<RegionTypeDetailsInfo.Result>()
    val filterStore = MainActivity.of(context).filterStore
    val timeSettingStore = MainActivity.of(context).timeSettingStore

    init {
        loadData()
    }

    fun loadData() {
        if (list.size >= pageNum * pageSize)
            return
        if (loadState == LoadMoreView.State.NOMORE) {
            return
        }
        loading set true
        val url = BiliApiService.getRegionTypeVideoList(tid, rankOrder, pageNum, pageSize, timeSettingStore.timeFromValue, timeSettingStore.timeToValue)
        var totalCount = 0 // 屏蔽前数量
        MiaoHttp.getJson<RegionTypeDetailsInfo>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { data -> data.result }
                .doOnNext { result ->
                    if (result.size < pageSize) {
                        loadState set LoadMoreView.State.NOMORE
                    }
                }
                .map { result ->
                    totalCount = result.size
                    result.filter {
                        filterStore.filterWord(it.title)
                                && filterStore.filterUpper(it.mid.toLong())
                    }
                }
                .subscribe({ result ->
                    list.addAll(result)
                    if (list.size < 10 && totalCount != result.size) {
                        loading set false
                        pageNum++
                        loadData()
                    }
                }, { err ->
                    loadState set LoadMoreView.State.FAIL
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
}