package com.a10miaomiao.bilimiao.ui.rank

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.view.Menu
import android.view.MenuItem
import com.a10miaomiao.bilimiao.entity.BangumiRankInfo
import com.a10miaomiao.bilimiao.entity.BiliMiaoRank
import com.a10miaomiao.bilimiao.entity.VideoRankInfo
import com.a10miaomiao.bilimiao.netword.ApiHelper
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class RankCategoryDetailsViewModel(
        val context: Context,
        var info: BiliMiaoRank,
        var id: Int
) : ViewModel() {

    val myFilter = info.filter.map {
        BiliMiaoRank.FilterItem(
                name = it.name,
                value = it.values[0].value
        )
    }

    val videoList = MiaoList<VideoRankInfo.VideoInfo>()
    val bangumioList = MiaoList<BangumiRankInfo.BangumiInfo>()
    var loading = MiaoLiveData<Boolean>(false)

    init {
        loadData()
    }

    fun loadData() {
        if (info.type == ConstantUtil.VIDEO)
            loadVideoData()
        else if (info.type == ConstantUtil.BANGUMI)
            loadBangumiData()
    }

    fun loadVideoData() {
        loading set  true
        val url = createUrl()
        val filterStore = MainActivity.of(context).filterStore
        MiaoHttp.getJson<VideoRankInfo>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { data ->
                    data.rank.list.filter {
                        filterStore.filterWord(it.title)
                                && filterStore.filterUpper(it.mid)
                    }
                }
                .subscribe({ list ->
                    loading set false
                    videoList.clear()
                    videoList.addAll(list)
                }, { e ->
                    loading set false
                    e.printStackTrace()
                })
    }

    fun loadBangumiData() {
        loading set true
        val url = createUrl()
        MiaoHttp.getJson<BangumiRankInfo>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                    DebugMiao.log(data)
                    loading set false
                    bangumioList.clear()
                    bangumioList.addAll(data.result.list)
                }, { e ->
                    loading set false
                    e.printStackTrace()
                })
    }

    private fun createUrl(): String {
        var url = info.url.replace("[category]", id.toString())
                .replace("[time]", ApiHelper.getTimeSpen().toString())
        myFilter.forEach {
            url = url.replace("[" + it.name + "]", it.value)
        }
        return url
    }

    fun createMenuItemClick(index: Int) = { menu: MenuItem ->
        myFilter[index].value = info.filter[index].values[menu.itemId - Menu.FIRST].value
        loadData()
    }

}