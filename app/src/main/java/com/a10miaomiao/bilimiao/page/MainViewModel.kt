package com.a10miaomiao.bilimiao.page

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.entity.ResultListInfo
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.miao.MiaoAdInfo
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.store.TimeSettingStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.collections.forEachWithIndex
import splitties.toast.toast
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.Exception

class MainViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val timeSettingStore: TimeSettingStore by instance()

    var title = "时光姬"
    var adInfo: MiaoAdInfo.AdBean? = null
    var regions = mutableListOf<RegionInfo>()
    var isBestRegion = false
    var prefs = PreferenceManager.getDefaultSharedPreferences(context)


    init {
        randomTitle()
        loadAdData()
        loadRegionData()
        viewModelScope.launch {
            timeSettingStore.connectUi(ui)
        }
    }

    fun getTimeText (): String {
        val state = timeSettingStore.state
        return state.timeFrom.getValue("-") + " 至 " + state.timeTo.getValue("-")
    }

    fun loadRegionData() {
        val isBestRegionNow = prefs.getBoolean("is_best_region", false)
        if (regions.size > 0 && isBestRegion == isBestRegionNow) {
            return
        }
        isBestRegion = isBestRegionNow
        // 加载分区列表
        viewModelScope.launch(Dispatchers.IO){
            try {
                val jsonStr = readRegionJson()!!
                val result = Gson().fromJson<ResultListInfo<RegionInfo>>(
                    jsonStr,
                    object : TypeToken<ResultListInfo<RegionInfo>>() {}.type
                )
                val data = result.data
                data.forEachWithIndex(::regionIcon)
                ui.setState {
                    regions = data.toMutableList()
                }
            } catch (e: Exception) {
                context.toast("读取分区列表遇到错误")
            }
            // 从网络读取最新版本的分区列表
            if (!isBestRegion) {
                getRegionsByNetword()
            }
        }
    }

    fun getRegionsByNetword () = viewModelScope.launch(Dispatchers.IO){
        try {
            val res = BiliApiService.regionAPI
                .regions()
                .awaitCall()
                .gson<ResultListInfo<RegionInfo>>()
            if (res.code == 0) {
                val regionList = res.data.filter { it.children != null && it.children.isNotEmpty() }
                ui.setState {
                    regions = regionList.toMutableList()
                }
                // 保存到本地
                writeRegionJson(
                    ResultListInfo(
                        code = 0,
                        data = regionList,
                        msg = "",
                    )
                )
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

    private fun writeRegionJson(region: ResultListInfo<RegionInfo>) {
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
     * 加载广告信息
     */
    private fun loadAdData() = viewModelScope.launch(Dispatchers.IO){
        try {
            val manager = context.packageManager
            val info = manager.getPackageInfo(context.packageName, 0)
            val longVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                info.versionCode.toLong()
            }
            val url = "https://bilimiao.10miaomiao.cn/miao/init?v=$longVersionCode"
            val res = MiaoHttp.request(url).call().gson<MiaoAdInfo>()
            if (res.code == 0) {
                ui.setState {
                    adInfo = res.data.ad
                }
                withContext(Dispatchers.Main) {
                    showUpdateDialog(res.data.version, longVersionCode)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showUpdateDialog(version: MiaoAdInfo.VersionBean, curVersionCode: Long) {
        // 当前版本大于等于最新版本不提示更新
        if (curVersionCode >= version.versionCode) {
            return
        }
        // 最新版已记录为不更新版本并且当前大于等于最低版本，不提示更新
        val sp = context.getSharedPreferences(Bilimiao.APP_NAME, Context.MODE_PRIVATE)
        val noUpdateVersionCode = sp.getLong("no_update_version_code", 0L)
        if (version.versionCode === noUpdateVersionCode && curVersionCode >= version.miniVersionCode) {
            return
        }
        val dialog = AlertDialog.Builder(context).apply {
            setTitle("有新版本：" + version.versionName)
            setMessage(version.content)
            setPositiveButton("去更新", null)
            if (curVersionCode >= version.miniVersionCode) {
                setNegativeButton("取消", null)
                setNeutralButton("不再提醒此版本") { dialog, which ->
                    sp.edit()
                        .putLong("no_update_version_code", version.versionCode)
                        .apply()
                }
            } else {
                // 小于最低版本，必须更新，对话框不能关闭
                setCancelable(false)
            }
        }.create()
        dialog.show()
        // 手动设置按钮点击事件，可阻止对话框自动关闭
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            var intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(version.url)
            context.startActivity(intent)
            if (curVersionCode >= version.miniVersionCode) {
                dialog.dismiss()
            }
        }
    }

    /**
     * 随机标题
     */
    private fun randomTitle() {
        val titles = arrayOf("时光姬", "时光基", "时光姬", "时光姬")
        val subtitles = arrayOf("ε=ε=ε=┏(゜ロ゜;)┛", "(　o=^•ェ•)o　┏━┓", "(/▽＼)", "ヽ(✿ﾟ▽ﾟ)ノ")
        val random = Random()
        ui.setState {
            title = titles[random.nextInt(titles.size)] + "  " + subtitles[random.nextInt(titles.size)]
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