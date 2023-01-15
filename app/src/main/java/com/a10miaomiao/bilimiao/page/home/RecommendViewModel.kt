package com.a10miaomiao.bilimiao.page.home

import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.card.v1.CardOuterClass
import bilibili.app.card.v1.Single
import bilibili.app.show.v1.PopularGrpc
import bilibili.app.show.v1.PopularOuterClass
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.home.HomeRecommendInfo
import com.a10miaomiao.bilimiao.comm.entity.home.RecommendCardInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.network.request
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.FilterStore
import kotlinx.coroutines.Dispatchers
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

    private val _idx get() = if (list.data.size == 0) {
        0
    } else {
        list.data[list.data.size - 1].idx
    }
    var list = PaginationInfo<RecommendCardInfo>()
    var triggered = false

    init {
        loadData(0)
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
                val itemsList = res.data.items.filter {
                   it.goto?.isNotEmpty() ?: false
                }
                ui.setState {
                    if (idx == 0L) {
                        list.data = itemsList.toMutableList()
                    } else {
                        list.data.addAll(itemsList)
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
