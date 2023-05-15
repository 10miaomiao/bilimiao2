package com.a10miaomiao.bilimiao.page.search.result

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.search.*
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.store.FilterStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class UpperResultViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val filterStore: FilterStore by instance()

    val keyword by lazy { fragment.requireArguments().getString(MainNavArgs.text, "") }

    var list = PaginationInfo<SearchUpperInfo>()
    var triggered = false

    init {
        loadData(1)
    }

    private fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            ui.setState {
                list.loading = true
            }

            val res = BiliApiService.searchApi
                .searchUpper(
                    keyword = keyword,
                    pageNum = pageNum,
                    pageSize = list.pageSize
                )
                .awaitCall()
                .gson<ResultInfo<SearchListInfo<SearchUpperInfo>>>()
            if (res.code == 0) {
                var result = res.data.items ?: listOf()
                var totalCount = 0 // 屏蔽前数量
                if (result.size < list.pageSize) {
                    ui.setState { list.finished = true }
                }
                totalCount = result.size
                result = result.filter {
                    filterStore.filterUpper(it.param)
                }
                ui.setState {
                    if (pageNum == 1) {
                        list.data = arrayListOf()
                    }
                    list.data.addAll(result)
                }
                list.pageNum = pageNum
                if (list.data.size < 10 && totalCount != result.size) {
                    _loadData(pageNum + 1)
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