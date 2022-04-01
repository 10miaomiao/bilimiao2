package com.a10miaomiao.bilimiao.page.bangumi

import android.content.Context
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class BangumiDetailViewModel (
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()

//    val info = MutableLiveData<Bangumi>()
//    val episodes = MiaoList<BangumiEpisode>()
//    val seasons = MiaoList<BangumiSeason>()
//    val loading = MutableLiveData<Boolean>()
//
//    val seasonsIndex = MutableLiveData<Int>()
//
//    init {
//        loadData()
//    }
//
//    fun loadData() {
//        loading.value = true
//        val url = BiliApiService.getBangumiInfo(sid)
//        DebugMiao.log(url)
//        MiaoHttp.getJson<ResultInfo2<Bangumi>>(url)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({ r ->
//                DebugMiao.log(r)
//                info.value = r.result
//                episodes.clear()
//                episodes.addAll(r.result.episodes)
//                seasons.clear()
//                seasons.addAll(r.result.seasons)
//                loading.value = false
//                setSasonsIndex()
//            }, { e ->
//                loading.value = false
//                e.printStackTrace()
//            })
//    }
//
//    fun setSasonsIndex(){
//        seasons.forEachIndexed { index, item ->
//            if (item.season_id == sid)
//                seasonsIndex.value = index
//        }
//    }
}