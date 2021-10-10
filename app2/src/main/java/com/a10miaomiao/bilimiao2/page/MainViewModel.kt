package com.a10miaomiao.bilimiao2.page

import android.content.Context
import android.util.DebugUtils
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.comm.entity.ResultListInfo
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao2.comm.MiaoBindingUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast
import java.lang.Exception

class MainViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()

    var count = 0
    var text = ""
    var items = mutableListOf("3", "4", "5")
    val regions = arrayListOf<RegionInfo>()

    init {
        getRegionsByNetword()
    }

    fun onClick (view: View) {
        ui.setState {
            items.add(text)
            text = ""
        }
    }

    fun getRegionsByNetword () = viewModelScope.launch(Dispatchers.IO){
        try {
            val res = BiliApiService.regionAPI
                .regions()
                .call()
                .gson<ResultListInfo<RegionInfo>>()
            if (res.code == 0) {
                val regionList = res.data.filter { it.children != null && it.children.isNotEmpty() }
                ui.setState {
                    regions.clear()
                    regions.addAll(regionList)
                }


//                // 保存到本地
//                writeRegionJson(
//                    Home.RegionData(
//                        data = regionList
//                    )
//                )
            } else {
                context.toast(res.msg)
            }

        } catch (e: Exception) {
            DebugMiao.loge(e)
        }
    }


}