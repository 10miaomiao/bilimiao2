package com.a10miaomiao.bilimiao.page.rank

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.show.v1.RankGrpc
import bilibili.app.show.v1.RankOuterClass
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultListInfo2
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.region.RegionTypeDetailsInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.network.request
import com.a10miaomiao.bilimiao.page.region.RegionDetailsFragment
import com.a10miaomiao.bilimiao.store.FilterStore
import com.a10miaomiao.bilimiao.store.TimeSettingStore
import com.a10miaomiao.bilimiao.widget.picker.DateModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class RankDetailViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val filterStore: FilterStore by instance()

    val rid by lazy { fragment.requireArguments().getInt(RegionDetailsFragment.TID) }

    var list = PaginationInfo<RankOuterClass.Item>()
    var triggered = false


    init {
        loadData()
    }

    private fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            ui.setState {
                list.loading = true
            }
            val result = (if (rid == 0) {
                val req = RankOuterClass.RankAllResultReq.newBuilder()
                    .setOrder("all")
                    .setPn(pageNum)
                    .setPs(list.pageSize)
                    .build()
                RankGrpc.getRankAllMethod().request(req).awaitCall()
            } else {
                val req = RankOuterClass.RankRegionResultReq.newBuilder()
                    .setRid(rid)
                    .setPn(pageNum)
                    .setPs(list.pageSize)
                    .build()
                RankGrpc.getRankRegionMethod().request(req).awaitCall()
            })
//            var totalCount = 0 // 屏蔽前数量
//            if (result.size < list.pageSize) {
//                ui.setState { list.finished = true }
//            }
//            totalCount = result.size
//            result = result.filter {
//                filterStore.filterWord(it.title)
//                        && filterStore.filterUpper(it.mid.toLong())
//            }
            ui.setState {
                if (pageNum == 1) {
                    list.data = arrayListOf()
                }
                list.data.addAll(result.itemsList)
            }
            list.pageNum = pageNum
//            if (list.data.size < 10 && totalCount != result.size) {
//                _loadData(pageNum + 1)
//            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                context.toast(e.message ?: "列表加载异常")
            }
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

    private fun _loadData(pageNum: Int = list.pageNum) {
        loadData(pageNum)
    }

    fun loadMode () {
        val (loading, finished, pageNum) = this.list
        if (!finished && !loading) {
            loadData(
                pageNum = pageNum + 1
            )
        }
    }

    fun refreshList() {
        ui.setState {
            list = PaginationInfo()
            triggered = true
            loadData()
        }
    }


}