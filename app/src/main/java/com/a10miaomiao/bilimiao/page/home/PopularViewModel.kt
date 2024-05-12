package com.a10miaomiao.bilimiao.page.home

import android.content.Context
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.card.v1.Card
import bilibili.app.card.v1.SmallCoverV5
import bilibili.app.show.v1.EntranceShow
import bilibili.app.show.v1.PopularGRPC
import bilibili.app.show.v1.PopularResultReq
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class PopularViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val filterStore: FilterStore by instance()

    private val lastIdx
        get() = if (list.data.size == 0) {
            0
        } else {
            list.data[list.data.size - 1].base?.idx ?: 0
        }
    var list = PaginationInfo<SmallCoverV5>()
    var topEntranceList = mutableListOf<EntranceShow>()
    var triggered = false

    init {
        loadData(0)
    }

    private fun loadData(
        idx: Long = lastIdx
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            ui.setState {
                list.loading = true
            }
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val carryToken = prefs.getBoolean("home_popular_carry_token", true)
            val req = PopularResultReq(
                idx = idx,
            )
            val result = BiliGRPCHttp.request {
                PopularGRPC.index(req)
            }.also {
                it.needToken = carryToken
            }.awaitCall()
            val itemsList = result.items
            val filterList = itemsList.mapNotNull {
                (it.item as? Card.Item.SmallCoverV5)?.value
            }.filter {
                val base = it.base
//                val upper = it?.up
                (base != null // && upper != null
                        && base.cardGoto == "av"
                        && filterStore.filterWord(base.title)
                        && filterStore.filterUpperName(it.rightDesc1)
                        && filterStore.filterTag(base.param, base.cardGoto)
                        )
            }
            ui.setState {
                val topItems = result.config?.topItems
                if (topItems != null) {
                    topEntranceList = topItems.toMutableList()
                }
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

    fun loadMode() {
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