package com.a10miaomiao.bilimiao.store

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo2
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import splitties.collections.forEachWithIndex
import splitties.toast.toast
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class RegionStore(override val di: DI) :
    ViewModel(), BaseStore<RegionStore.State> {

    data class State(
        var regions: MutableList<RegionInfo> = mutableListOf()
    )

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    private var networkTime = 0L

    override fun init(context: Context) {
        super.init(context)
        // 加载分区列表
        SettingPreferences.launch(viewModelScope, Dispatchers.IO) {
            context.dataStore.data.map {
                it[IsBestRegion] ?: false
            }.collect {
                loadRegionData(context, it)
            }
        }
    }

    suspend fun loadRegionData(
        context: Context,
        isBestRegion: Boolean,
    ) {
        // 加载分区列表
        try {
            val jsonStr = readRegionJson(context, isBestRegion)!!
            val result = Gson().fromJson<ResultInfo<List<RegionInfo>>>(
                jsonStr,
                object : TypeToken<ResultInfo<List<RegionInfo>>>() {}.type
            )
            val data = result.data
            data.forEachWithIndex(::regionIcon)
            setState {
                regions = data.toMutableList()
            }
        } catch (e: Exception) {
            context.toast("读取分区列表遇到错误")
        }
        // 从网络读取最新版本的分区列表
        if (!isBestRegion && System.currentTimeMillis() - networkTime > 3600000) {
            getRegionsByNetwork(context)
        }
    }

    suspend fun getRegionsByNetwork(context: Context) {
        try {
            val res = BiliApiService.regionAPI
                .regions()
                .awaitCall()
                .gson<ResultInfo<List<RegionInfo>>>()
            if (res.isSuccess) {
                val regionList = res.data.filter { it.children != null && it.children.isNotEmpty() }
                setState {
                    regions = regionList.toMutableList()
                }
                networkTime = System.currentTimeMillis()
                // 保存到本地
                writeRegionJson(
                    context,
                    ResultInfo(
                        code = 0,
                        data = regionList,
                        message = "",
                        ttl = 0,
                    )
                )
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 读取assets下的json数据
     */
    private fun readRegionJson(
        context: Context,
        isBestRegion: Boolean,
    ): String? {
        try {
            val inputStream = if (isBestRegion || !File(context.filesDir, "region.json").exists()) {
                context.assets.open("region.json")
            } else {
                context.openFileInput("region.json")
            }
            val br = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var str: String? = br.readLine()
            while (str != null) {
                stringBuilder.append(str)
                str = br.readLine()
            }
            return stringBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun writeRegionJson(context: Context, region: ResultInfo<List<RegionInfo>>) {
        try {
            val jsonStr = Gson().toJson(region)
            val outputStream = context.openFileOutput("region.json", Context.MODE_PRIVATE);
            outputStream.write(jsonStr.toByteArray());
            outputStream.close();
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 分区图标
     */
    private fun regionIcon(index: Int, item: RegionInfo) {
        if (item.logo == null) {
            item.icon = intArrayOf(
                R.drawable.ic_region_fj, R.drawable.ic_region_fj_domestic, R.drawable.ic_region_dh,
                R.drawable.ic_region_yy, R.drawable.ic_region_wd, R.drawable.ic_region_yx,
                R.drawable.ic_region_kj, R.drawable.ic_region_sh, R.drawable.ic_region_gc,
                R.drawable.ic_region_ss, R.drawable.ic_region_ad, R.drawable.ic_region_yl,
                R.drawable.ic_region_ys, R.drawable.ic_region_dy, R.drawable.ic_region_dsj
            )[index]
        }
    }
}