package com.a10miaomiao.bilimiao.ui.search

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.support.v4.app.Fragment
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.entity.SearchBangumiItems
import com.a10miaomiao.bilimiao.entity.SearchListData
import com.a10miaomiao.bilimiao.entity.SearchUpperItems
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class UpperResultViewModel(val fragment: Fragment) : ViewModel() {

    val list = MiaoList<SearchUpperItems>()
    val loading = MutableLiveData<Boolean>()
    val loadState = MutableLiveData<LoadMoreView.State>()
    var keyword = ""
    var pageNum = 1
    val pageSize = 10

    init {
        loading.value = true
        loadState.value = LoadMoreView.State.LOADING
        SearchFragment.keyword.observe(fragment, Observer {
            if (keyword != it!!) {
                keyword = it
                refreshList()
            }
        })
        loadData()
    }

    fun loadData() {
        val url = BiliApiService.getSearchUpper(keyword, pageNum, pageSize)
        loading.value = true
        MiaoHttp.getJson<ResultInfo<SearchListData<SearchUpperItems>>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res ->
                    loading.value = false
                    val items = res.data.items
                    list.addAll(items)
                    if (items.size < pageSize) {
                        loadState.value = LoadMoreView.State.NOMORE
                    }
                }, { err ->
                    loading.value = false
                    loadState.value = LoadMoreView.State.FAIL
                    err.printStackTrace()
                })
    }

    fun refreshList() {
        pageNum = 1
        list.clear()
        loadState.value = LoadMoreView.State.LOADING
        loadData()
    }
}