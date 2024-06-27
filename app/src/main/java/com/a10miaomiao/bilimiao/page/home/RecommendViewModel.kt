package com.a10miaomiao.bilimiao.page.home

import android.content.Context
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.home.HomeRecommendInfo
import com.a10miaomiao.bilimiao.comm.entity.home.RecommendCardInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class RecommendViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val filterStore: FilterStore by instance()
    var listStyle = 0

    private val _idx get() = if (list.data.size == 0) {
        0
    } else {
        list.data[list.data.size - 1].idx
    }
    var list = PaginationInfo<RecommendCardInfo>()
    var triggered = false

    init {
        viewModelScope.launch {
            loadData(1)
            SettingPreferences.run {
                context.dataStore.data.map {
                    it[HomeRecommendListStyle] ?: 0
                }
            }.collect {
                listStyle = it
            }
        }
    }

    private fun loadData(
        idx: Long = _idx
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            ui.setState {
                list.loading = true
            }
            val res = BiliApiService.homeApi.recommendList(
                idx = idx,
            ).awaitCall().gson<ResultInfo<HomeRecommendInfo>>()
            if (res.isSuccess) {
                val itemsList = res.data.items
                val filterList = itemsList.filter {
                    (it.goto?.isNotEmpty() ?: false)
                        && filterStore.filterWord(it.title)
                        && filterStore.filterUpper(it.args.up_id ?: "-1")
                        && filterStore.filterTag(it.param, it.card_goto)
                }
                ui.setState {
                    if (idx == 0L) {
                        list.data = filterList.toMutableList()
                    } else {
                        list.data.addAll(filterList)
                    }
                    if (itemsList.isEmpty()) {
                        list.finished = true
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    context.toast(res.message)
                }
                throw Exception(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ui.setState {
                list.fail = true
            }
        } finally {
            ui.setState {
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
            loadData(_idx)
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
