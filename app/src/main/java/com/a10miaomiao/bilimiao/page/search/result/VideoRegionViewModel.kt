package com.a10miaomiao.bilimiao.page.search.result

import android.content.Context
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultListInfo
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.collections.forEachWithIndex
import splitties.toast.toast
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class VideoRegionViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()

    val checkRegionId by lazy { fragment.requireArguments().getString(MainNavGraph.args.id, "") }
    var regions = mutableListOf<RegionInfo>()
    var isBestRegion = false
    var prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val allRegionInfo = RegionInfo(
        tid =0,
        reid = 0,
        logo = null,
        icon = null,
        name = "全部分区",
        type = 0,
        uri = "",
        children = listOf()
    )

    init {
        loadRegionData()
    }

    fun loadRegionData() {
        val isBestRegionNow = prefs.getBoolean("is_best_region", false)
        if (regions.size > 0 && isBestRegion == isBestRegionNow) {
            return
        }
        isBestRegion = isBestRegionNow
        // 加载分区列表
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonStr = readRegionJson()!!
                val result = Gson().fromJson<ResultListInfo<RegionInfo>>(
                    jsonStr,
                    object : TypeToken<ResultListInfo<RegionInfo>>() {}.type
                )
                val data = result.data
                data.forEachWithIndex(::regionIcon)
                ui.setState {
                    regions = mutableListOf(allRegionInfo, *data.toTypedArray())
                }
            } catch (e: Exception) {
                context.toast("读取分区列表遇到错误")
            }
            // 从网络读取最新版本的分区列表
            if (!isBestRegion && regions.size == 0) {
                getRegionsByNetword()
            }
        }
    }

    fun getRegionsByNetword() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = BiliApiService.regionAPI
                .regions()
                .awaitCall()
                .gson<ResultListInfo<RegionInfo>>()
            if (res.code == 0) {
                val regionList = res.data.filter { it.children != null && it.children.isNotEmpty() }
                ui.setState {
                    regions = mutableListOf(allRegionInfo, *regionList.toTypedArray())
                }
            } else {
                context.toast(res.msg)
            }
        } catch (e: Exception) {
            DebugMiao.loge(e)
        }
    }

    /**
     * 读取assets下的json数据
     */
    private fun readRegionJson(): String? {
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