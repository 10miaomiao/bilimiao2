package com.a10miaomiao.bilimiao.ui.region

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.a10miaomiao.bilimiao.entity.RegionTypeDetailsInfo
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.bilimiao.ui.commponents.model.DateModel
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.bilimiao.utils.RxBus
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import com.a10miaomiao.miaoandriod.adapter.MiaoRecyclerViewAdapter
import com.a10miaomiao.miaoandriod.binding.MiaoViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class RegionDetailsViewModel(val context: Context, val tid: Int) : MiaoViewModel() {
    val timeFrom = DateModel(this)
    val timeTo = DateModel(this)

    var pageNum = 1
    val pageSize = 10
    var rankOrder = "click"  //排行依据

    var loading by miao(false)
    var loadState by miao(LoadMoreView.State.LOADING)

    var list = MiaoList<RegionTypeDetailsInfo.Result>()

    private var subscriber = RxBus.getInstance().on(ConstantUtil.TIME_CHANGE) {
        val timeFrom = DateModel(this).read(context!!, ConstantUtil.TIME_FROM)
        val timeTo = DateModel(this).read(context!!, ConstantUtil.TIME_TO)
        if (this.timeFrom.diff(timeFrom) || this.timeTo.diff(timeTo)) {
            this.timeFrom.setValue(timeFrom)
            this.timeTo.setValue(timeTo)
            refreshList()
        }
    }

    init {
        timeFrom.read(context!!, ConstantUtil.TIME_FROM)
        timeTo.read(context!!, ConstantUtil.TIME_TO)
        loadData()
    }

    override fun onCleared() {
        super.onCleared()
        subscriber?.dispose()
    }

    fun loadData() {
        if (list.size >= pageNum * pageSize)
            return
        if (loadState == LoadMoreView.State.NOMORE) {
            return
        }
        loading = true
        val url = BiliApiService.getRegionTypeVideoList(tid, rankOrder, pageNum, pageSize, timeFrom.getValue(), timeTo.getValue())
        MiaoHttp.getJson<RegionTypeDetailsInfo>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                    loading = false
                    list.addAll(data.result)
                    DebugMiao.log(data.result.size )
                    if (data.result.size < pageSize) {
                        loadState = LoadMoreView.State.NOMORE
                    }
                }, { err ->
                    loading = false
                    loadState = LoadMoreView.State.FAIL
                    err.printStackTrace()
                })
    }

    fun refreshList() {
        pageNum = 1
        list.clear()
        loadState = LoadMoreView.State.LOADING
        loadData()
    }
}