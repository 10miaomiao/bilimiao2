package com.a10miaomiao.bilimiao.page.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.entity.ResultListInfo
import com.a10miaomiao.bilimiao.comm.entity.miao.MiaoAdInfo
import com.a10miaomiao.bilimiao.comm.entity.miao.MiaoSettingInfo
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.RegionStore
import com.a10miaomiao.bilimiao.comm.store.TimeSettingStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
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
            val url = "https://bilimiao.10miaomiao.cn/miao/init?v=$longVersionCode"
            val res = MiaoHttp.request(url).call().gson<MiaoAdInfo>()
            if (res.code == 0) {
                ui.setState {
                    adInfo = res.data.ad
                }
                withContext(Dispatchers.Main) {
                    saveSettingList(res.data.settingList)
                    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                    val autoCheckUpdate = prefs.getBoolean("auto_check_update", true)
                    if (autoCheckUpdate) {
                        showUpdateDialog(res.data.version, longVersionCode)
                    }
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