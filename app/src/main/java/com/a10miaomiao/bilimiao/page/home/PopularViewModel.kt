package com.a10miaomiao.bilimiao.page.home

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.account.fission.v1.FissionGrpc
import bilibili.account.fission.v1.FissionOuterClass
import bilibili.app.card.v1.CardOuterClass
import bilibili.app.card.v1.Single
import bilibili.app.show.v1.PopularGrpc
import bilibili.app.show.v1.PopularOuterClass
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.search.SearchArchiveInfo
import com.a10miaomiao.bilimiao.comm.entity.search.SearchResultInfo
import com.a10miaomiao.bilimiao.comm.entity.search.SearchVideoInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.network.request
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.FilterStore
import com.a10miaomiao.bilimiao.widget.menu.CheckPopupMenu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

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
            val req = PopularOuterClass.PopularResultReq.newBuilder()
                .setIdx(idx)
                .build()
            val result = PopularGrpc.getIndexMethod()
                .request(req)
                .awaitCall()
            val itemsList = result.itemsList.filter {
                it.itemCase == CardOuterClass.Card.ItemCase.SMALL_COVER_V5
                        && it.smallCoverV5 != null
                        && it.smallCoverV5.base.cardGoto == "av"
            }.map {
                it.smallCoverV5
            }
            val topItems = result.config.topItemsList
            ui.setState {
                topEntranceList = topItems
                if (idx == 0L) {
                    list.data = itemsList.toMutableList()
                } else {
                    list.data.addAll(itemsList)
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
            loadData(0)
        }
    }
}