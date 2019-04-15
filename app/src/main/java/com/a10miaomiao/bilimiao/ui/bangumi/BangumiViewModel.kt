package com.a10miaomiao.bilimiao.ui.bangumi

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.entity.ResultInfo2
import com.a10miaomiao.bilimiao.entity.bangumi.Bangumi
import com.a10miaomiao.bilimiao.entity.bangumi.BangumiEpisode
import com.a10miaomiao.bilimiao.entity.bangumi.BangumiSeason
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class BangumiViewModel(val context: Context, val sid: String) : ViewModel() {

    val info = MutableLiveData<Bangumi>()
    val episodes = MiaoList<BangumiEpisode>()
    val seasons = MiaoList<BangumiSeason>()
    val loading = MutableLiveData<Boolean>()

    val seasonsIndex = MutableLiveData<Int>()

    init {
        loadData()
    }

    fun loadData() {
        loading.value = true
        val url = BiliApiService.getBangumiInfo(sid)
        DebugMiao.log(url)
        MiaoHttp.getJson<ResultInfo2<Bangumi>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ r ->
                    DebugMiao.log(r)
                    info.value = r.result
                    episodes.clear()
                    episodes.addAll(r.result.episodes)
                    seasons.clear()
                    seasons.addAll(r.result.seasons)
                    loading.value = false
                    setSasonsIndex()
                }, { e ->
                    loading.value = false
                    e.printStackTrace()
                })
    }

    fun setSasonsIndex(){
        seasons.forEachIndexed { index, item ->
            if (item.season_id == sid)
                seasonsIndex.value = index
        }
    }
}