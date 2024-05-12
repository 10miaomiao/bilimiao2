package com.a10miaomiao.bilimiao.page.rank

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.show.v1.RankAllResultReq
import bilibili.app.show.v1.RankGRPC
import bilibili.app.show.v1.RankRegionResultReq
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.page.region.RegionDetailsFragment
import com.a10miaomiao.bilimiao.comm.store.FilterStore
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

    var list = PaginationInfo<bilibili.app.show.v1.Item>()
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
            val result = BiliGRPCHttp.request {
                if (rid == 0) {
                    val req = RankAllResultReq(
                        order = "all",
                        pn = pageNum,
                        ps = list.pageSize
                    )
                    RankGRPC.rankAll(req)
                } else {
                    val req = RankRegionResultReq(
                        rid = rid,
                        pn = pageNum,
                        ps = list.pageSize
                    )
                    RankGRPC.rankRegion(req)
                }
            }.awaitCall()
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
                list.data.addAll(result.items)
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