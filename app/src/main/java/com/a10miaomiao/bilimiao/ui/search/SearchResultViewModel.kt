package com.a10miaomiao.bilimiao.ui.search

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import com.a10miaomiao.bilimiao.entity.*
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.toast
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class SearchResultViewModel(
        val context: Context,
        val fragment: Fragment
) : ViewModel() {

    val list = MiaoList<Archive>()
    val loading = MutableLiveData<Boolean>()
    val loadState = MutableLiveData<LoadMoreView.State>()

    val rankOrdersNameList = arrayListOf("默认", "相关度", "新发布", "播放多", "弹幕多", "评论多", "收藏多")
    val durationNameList = arrayListOf("全部", "0-10分钟", "10-30分钟", "30-60分钟", "60分钟+")
    val regionNameList = MiaoList<String>().apply {
        add("全站")
    }
    val rankOrdersValueList = arrayListOf("default", "ranklevel", "pubdate", "click", "dm", "scores", "stow")
    val regionValueList = arrayListOf(0)
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
        loading.value = false
        loadState.value = LoadMoreView.State.LOADING
        rankOrdersIndex.value = 0
        durationIndex.value = 0
        regionIndex.value = 0
        updateFilter()
        loadData()
        loadRegion()
    }

    fun updateFilter() {
        val text = "${rankOrdersNameList[rankOrdersIndex.value!!]} · ${durationNameList[durationIndex.value!!]} · ${regionNameList[regionIndex.value!!]}>>>"
        if (text != filterName.value) {
            filterName.value = text
            refreshList()
        }
    }

    private fun loadRegion() {
        Observable.just(readRegionJson())
                .map { Gson().fromJson(it, Home.RegionData::class.java) }
                .map { it.data }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    regionNameList.clear()
                    regionNameList.add("全站")
                    regionNameList.addAll(
                            it.map { it.name }
                    )
                    regionValueList.clear()
                    regionValueList.add(0)
                    regionValueList.addAll(
                            it.map { it.tid }
                    )
                }, {
                    context.toast("读取分区信息遇到错误")
                })
    }

    fun loadData() {
        loading.value = true
        val filterStore = Store.from(context).filterStore
        var totalCount = 0 // 屏蔽前数量
        MiaoHttp.getJson<ResultInfo<SearchData<SearchItems>>>(getUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { r -> r.data.items.archive }
                .doOnNext { archive ->
                    if (archive.size < pageSize) {
                        loadState.value = LoadMoreView.State.NOMORE
                    }
                }
                .map { result ->
                    totalCount = result.size
                    result.filter {
                        filterStore.filterWord(it.title)
                                && filterStore.filterUpper(it.mid)
                    }
                }
                .subscribe({ archive ->
                    list.addAll(archive)
                    if (list.size < 10 && totalCount != archive.size) {
                        pageNum++
                        loadData()
                    }
                }, { err ->
                    loadState.value = LoadMoreView.State.FAIL
                    err.printStackTrace()
                }, {
                    loading.value = false
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

    /**
     * 读取assets下的json数据
     */

    private fun readRegionJson(): String? {
        try {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val isBestRegion = prefs.getBoolean("is_best_region", false)
            val inputStream = if (isBestRegion || !File(context.filesDir, "region.json").exists()) {
                context.assets.open("region.json")
            } else {
                context.openFileInput("region.json")
            }
            val br = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var str: String? = br.readLine()
            while (str != null) {
                stringBuilder.append(str)
                str = br.readLine()
            }
            return stringBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

}