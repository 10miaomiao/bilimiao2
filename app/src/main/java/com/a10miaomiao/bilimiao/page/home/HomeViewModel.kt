package com.a10miaomiao.bilimiao.page.home

import android.content.*
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.entity.miao.MiaoAdInfo
import com.a10miaomiao.bilimiao.comm.entity.miao.MiaoSettingInfo
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.TimeSettingStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.store.RegionStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.io.IOException
import java.util.*

class HomeViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val timeSettingStore: TimeSettingStore by instance()
    val regionStore: RegionStore by instance()
    val userStore: UserStore by instance()

    var title = "时光姬"
    var adInfo: MiaoAdInfo.AdBean? = null

    init {
        loadAdData()
    }

    fun getTimeText(): String {
        val state = timeSettingStore.state
        return state.timeFrom.getValue("-") + " 至 " + state.timeTo.getValue("-")
    }

    private suspend fun getMiaoInitData(version: String): MiaoAdInfo {
        val sp = context.getSharedPreferences(Bilimiao.APP_NAME, Context.MODE_PRIVATE)
        val calendar = GregorianCalendar()
        val curDate = version + calendar.get(Calendar.YEAR) +
                calendar.get(Calendar.MONTH) +
                calendar.get(Calendar.DATE)
        val lastDate = sp.getString("miao_init_request_date", "")
        if (curDate == lastDate) {
            // 同一天不重复请求init接口，节省服务器资源
            val inputStream = context.openFileInput("miaoInit.json")
            val jsonStr = inputStream.reader().readText()
            return Gson().fromJson(jsonStr, MiaoAdInfo::class.java)
        }
        val url = "https://bilimiao.10miaomiao.cn/miao/init?v=$version"
        val res = MiaoHttp.request(url).awaitCall().gson<MiaoAdInfo>()
        val cacheJsonStr = Gson().toJson(res)
        sp.edit().putString("miao_init_request_date", curDate).apply()
        val outputStream = context.openFileOutput("miaoInit.json", Context.MODE_PRIVATE);
        outputStream.write(cacheJsonStr.toByteArray());
        outputStream.close();
        return res
    }

    /**
     * 加载广告信息
     */
    private fun loadAdData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val manager = context.packageManager
            val info = manager.getPackageInfo(context.packageName, 0)
            val longVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                info.versionCode.toLong()
            }
            val res = getMiaoInitData(longVersionCode.toString())
            if (res.code == 0) {
                val adData = res.data.ad
                ui.setState {
                    adInfo = adData
                }
                withContext(Dispatchers.Main) {
                    saveSettingList(res.data.settingList)
                    val (autoCheckUpdate, ignoreUpdateVersionCode) = SettingPreferences.mapData(context) {
                        Pair(
                            it[IsAutoCheckVersion] ?: true,
                            it[IgnoreUpdateVersionCode] ?: 0L
                        )
                    }
                    val version = res.data.version
                    if (autoCheckUpdate
                        && version.versionCode > longVersionCode
                        && version.versionCode != ignoreUpdateVersionCode
                    ) {
                        showUpdateDialog(version, longVersionCode)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showUpdateDialog(version: MiaoAdInfo.VersionBean, curVersionCode: Long) {
        val dialog = MaterialAlertDialogBuilder(context).apply {
            setTitle("有新版本：" + version.versionName)
            setMessage(version.content)
            setPositiveButton("去更新", null)
            if (curVersionCode >= version.miniVersionCode) {
                setNegativeButton("取消", null)
                setNeutralButton("不再提醒此版本") { dialog, which ->
                    setIgnoreUpdateVersion(version.versionCode)
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

    fun setIgnoreUpdateVersion(versionCode: Long) {
        viewModelScope.launch {
            SettingPreferences.edit(context) {
                it[IgnoreUpdateVersionCode] = versionCode
            }
        }
    }

    fun saveSettingList(settingList: List<MiaoSettingInfo>) {
        try {
            val jsonStr = Gson().toJson(settingList)
            val outputStream = context.openFileOutput("settingList.json", Context.MODE_PRIVATE);
            outputStream.write(jsonStr.toByteArray());
            outputStream.close();
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 随机标题
     */
    fun randomTitle() {
        val titles = arrayOf("时光姬", "时光基", "时光姬", "时光姬")
        val subtitles = arrayOf("ε=ε=ε=┏(゜ロ゜;)┛", "(　o=^•ェ•)o　┏━┓", "(/▽＼)", "ヽ(✿ﾟ▽ﾟ)ノ")
        val random = Random()
        ui.setState {
            title =
                titles[random.nextInt(titles.size)] + "  " + subtitles[random.nextInt(titles.size)]
        }
    }

}