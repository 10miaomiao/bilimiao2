package com.a10miaomiao.bilimiao.page.user.bangumi

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo2
import com.a10miaomiao.bilimiao.comm.entity.bangumi.MyBangumiInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.store.UserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class MyBangumiViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val userStore: UserStore by instance()

    var triggered = false
    var list = PaginationInfo<MyBangumiInfo>()

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
            val res = BiliApiService.userApi
                .followBangumi(
                    pageNum = pageNum,
                    pageSize = list.pageSize,
                )
                .awaitCall()
                .gson<ResultInfo2<List<MyBangumiInfo>>>()
            if (res.code == 0) {
                val result = res.result
                if (result.size < list.pageSize) {
                    ui.setState { list.finished = true }
                }
                ui.setState {
                    if (pageNum == 1) {
                        list.data = arrayListOf()
                    }
                    list.data.addAll(result)
                }
                list.pageNum = pageNum
            } else {
                context.toast(res.message)
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

    private fun _loadData() {
        loadData()
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