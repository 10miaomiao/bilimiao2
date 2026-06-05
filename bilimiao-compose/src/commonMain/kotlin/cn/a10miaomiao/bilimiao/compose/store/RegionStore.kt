package cn.a10miaomiao.bilimiao.compose.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.appDataStore
import com.a10miaomiao.bilimiao.comm.datastore.launch
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import bilimiao.bilimiao_compose.generated.resources.Res
import org.kodein.di.DI
import java.io.File
import java.io.IOException

class RegionStore(override val di: DI) :
    ViewModel(), BaseStore<RegionStore.State> {

    data class State(
        var regions: MutableList<RegionInfo> = mutableListOf()
    )

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    private val filesDir = PlatformProviders.context.filesDir

    private var networkTime = 0L

    override fun init() {
        super.init()
        SettingPreferences.launch(viewModelScope, Dispatchers.IO) {
            appDataStore.data.map {
                it[SettingPreferences.IsBestRegion] ?: false
            }.collect {
                loadRegionData(it)
            }
        }
    }

    suspend fun loadRegionData(
        isBestRegion: Boolean,
    ) {
        try {
            val jsonStr = readRegionJson(isBestRegion)!!
            val result = MiaoJson.fromJson<ResponseData<List<RegionInfo>>>(jsonStr)
            val data = result.requireData()
            data.forEachIndexed(::regionIcon)
            setState {
                regions = data.toMutableList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            GlobalToaster.show("读取分区列表遇到错误")
        }
        if (!isBestRegion && System.currentTimeMillis() - networkTime > 3600000) {
            getRegionsByNetwork()
        }
    }

    suspend fun getRegionsByNetwork() {
        try {
            val res = BiliApiService.regionAPI
                .regions()
                .awaitCall()
                .json<ResponseData<List<RegionInfo>>>()
            if (res.isSuccess) {
                val regionList = res.requireData().filter { it.children?.isNotEmpty() == true }
                setState {
                    regions = regionList.toMutableList()
                }
                networkTime = System.currentTimeMillis()
                writeRegionJson(
                    ResponseData(
                        code = 0,
                        data = regionList,
                        message = "",
                        ttl = 0,
                    )
                )
            } else {
                withContext(Dispatchers.Main) {
                    GlobalToaster.show(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun readRegionJson(
        isBestRegion: Boolean,
    ): String? {
        return try {
            val cachedFile = File(filesDir, "region.json")
            if (isBestRegion || !cachedFile.exists()) {
                val bytes = Res.readBytes("files/region.json")
                String(bytes)
            } else {
                cachedFile.readText()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun writeRegionJson(region: ResponseData<List<RegionInfo>>) {
        try {
            val jsonStr = MiaoJson.toJson(region)
            val file = File(filesDir, "region.json")
            file.writeText(jsonStr)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private val regionIconNames = arrayOf(
            "ic_region_fj", "ic_region_fj_domestic", "ic_region_dh",
            "ic_region_yy", "ic_region_wd", "ic_region_yx",
            "ic_region_kj", "ic_region_sh", "ic_region_gc",
            "ic_region_ss", "ic_region_ad", "ic_region_yl",
            "ic_region_ys", "ic_region_dy", "ic_region_dsj"
        )
    }

    private fun regionIcon(index: Int, item: RegionInfo) {
        if (item.logo == null && index in regionIconNames.indices) {
            item.icon = regionIconNames[index]
        }
    }
}
