package com.a10miaomiao.bilimiao.ui.search

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.support.v4.app.Fragment
import com.a10miaomiao.bilimiao.entity.Archive
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.entity.SearchData
import com.a10miaomiao.bilimiao.entity.SearchItems
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SearchResultViewModel(val fragment: Fragment) : ViewModel() {

    val list = MiaoList<Archive>()
    val loading = MutableLiveData<Boolean>()
    val loadState = MutableLiveData<LoadMoreView.State>()

    val rankOrdersNameList = arrayListOf("默认", "相关度", "新发布", "播放多", "弹幕多", "评论多", "收藏多")
    val durationNameList = arrayListOf("全部", "0-10分钟", "10-30分钟", "30-60分钟", "60分钟+")
    val regionNameList = arrayListOf("全站", "番剧", "国创", "动画"
            , "音乐", "舞蹈", "游戏", "科技"
            , "生活", "鬼畜", "时尚", "广告"
            , "娱乐", "电影", "电视剧")
    val rankOrdersValueList = arrayListOf("default", "ranklevel", "pubdate", "click", "dm", "scores", "stow")
    val regionValueList = arrayListOf(0, 13, 167, 1,
            3, 129, 4, 36,
            160, 119, 155, 165,
            5, 23, 11)
    val rankOrdersIndex = MutableLiveData<Int>()
    val durationIndex = MutableLiveData<Int>()
    val regionIndex = MutableLiveData<Int>()

    val filterName = MutableLiveData<String>()
    var keyword = ""
    var pageNum = 1
    val pageSize = 10

    init {
        SearchFragment.keyword.observe(fragment, Observer {
            if (keyword != it!!) {
                keyword = it
                refreshList()
            }
        })
        loadState.value = LoadMoreView.State.LOADING
        rankOrdersIndex.value = 0
        durationIndex.value = 0
        regionIndex.value = 0
        updateFilter()
        loadData()
    }

    fun updateFilter() {
        val text = "${rankOrdersNameList[rankOrdersIndex.value!!]} · ${durationNameList[durationIndex.value!!]} · ${regionNameList[regionIndex.value!!]}>>>"
        if (text != filterName.value) {
            filterName.value = text
            refreshList()
        }
    }

    fun loadData() {
        loading.value = true
        MiaoHttp.getJson<ResultInfo<SearchData<SearchItems>>>(getUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res ->
                    loading.value = false
                    val archive = res.data.items.archive
                    list.addAll(archive)
                    if (archive.size < pageSize) {
                        loadState.value = LoadMoreView.State.NOMORE
                    }
                }, { err ->
                    loading.value = false
                    loadState.value = LoadMoreView.State.FAIL
                    err.printStackTrace()
                })
    }

    private fun getUrl(): String {
        val rankOrder = rankOrdersValueList[rankOrdersIndex.value!!]
        val duration = durationIndex.value!!
        val region = regionValueList[regionIndex.value!!]
        return BiliApiService.getSearchArchive(keyword, pageNum, pageSize, rankOrder, duration, region)
    }

    fun refreshList() {
        pageNum = 1
        list.clear()
        loadState.value = LoadMoreView.State.LOADING
        loadData()
    }

}