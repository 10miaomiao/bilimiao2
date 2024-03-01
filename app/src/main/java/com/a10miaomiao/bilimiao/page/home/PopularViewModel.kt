package com.a10miaomiao.bilimiao.page.home

import android.content.Context
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log.Logger
import bilibili.app.card.v1.CardOuterClass
import bilibili.app.card.v1.Single
import bilibili.app.show.v1.PopularGrpc
import bilibili.app.show.v1.PopularOuterClass
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.apis.VideoAPI
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.network.request
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.util.Locale

class PopularViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val filterStore: FilterStore by instance()

    private val lastIdx get() = if (list.data.size == 0) {
        0
    } else {
        list.data[list.data.size - 1].base.idx
    }
    var list = PaginationInfo<Single.SmallCoverV5>()
    var topEntranceList = mutableListOf<PopularOuterClass.EntranceShow>()
    var triggered = false

    init {
        loadData(0)
    }

    private fun loadData(
        idx: Long = lastIdx
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            ui.setState {
                list.loading = true
            }
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val carryToken = prefs.getBoolean("home_popular_carry_token", true)
            val req = PopularOuterClass.PopularResultReq.newBuilder()
                .setIdx(idx)
                .build()
            val result = PopularGrpc.getIndexMethod()
                .request(req){ needToken = carryToken }
                .awaitCall()
            val itemsList = result.itemsList
            val filterList = itemsList.filter {
                var notHide = it.itemCase == CardOuterClass.Card.ItemCase.SMALL_COVER_V5
                        && it.smallCoverV5 != null
                        && it.smallCoverV5.base.cardGoto == "av"
                        && filterStore.filterWord(it.smallCoverV5.base.title)
                        && filterStore.filterUpper(it.smallCoverV5.up.id)
                if (filterStore.filterTagCount != 0
                    && it.itemCase == CardOuterClass.Card.ItemCase.SMALL_COVER_V5
                    && it.smallCoverV5 != null) {
                    notHide = notHide && when (it.smallCoverV5.base.cardGoto.lowercase()) {
                        "av" -> {
                            val tag = BiliApiService.videoAPI.info(it.smallCoverV5.base.param, it.smallCoverV5.base.cardGoto).call().gson<ResultInfo<VideoInfo>>().data.tag
                            filterStore.filterTag(tag)
                        }
                        else -> true
                    }
                }
                notHide
            }.map {
                it.smallCoverV5
            }
            val topItems = result.config.topItemsList
            ui.setState {
                topEntranceList = topItems
                if (idx == 0L) {
                    list.data = filterList.toMutableList()
                } else {
                    list.data.addAll(filterList)
                }
                if (itemsList.isEmpty()) {
                     list.finished = true
                }
                list.loading = false
                triggered = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ui.setState {
                list.fail = true
                list.loading = false
                triggered = false
            }
        }
    }


    fun tryAgainLoadData() {
        val (loading, finished) = this.list
        if (!finished && !loading) {
            loadData()
        }
    }

    fun loadMode () {
        val (loading, finished, pageNum) = this.list
        if (!finished && !loading) {
            loadData(lastIdx)
        }
    }

    fun refreshList() {
        ui.setState {
            list = PaginationInfo()
            triggered = true
        }
        loadData(0)
    }
}