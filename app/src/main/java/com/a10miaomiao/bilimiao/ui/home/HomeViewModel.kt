package com.a10miaomiao.bilimiao.ui.home

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.view.View
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.entity.Home
import com.a10miaomiao.bilimiao.entity.MiaoAdInfo
import com.a10miaomiao.bilimiao.entity.ResultListInfo
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.bilimiao.utils.SettingUtil
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.alert
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.toast
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*


class HomeViewModel(
        val context: Context
) : ViewModel() {

    var title = MiaoLiveData("时光姬")
    var adInfo = MiaoLiveData<MiaoAdInfo.AdBean?>(null)
    var region = MiaoList<Home.Region>()
    var isBestRegion = false
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    // 判断是否选择了使用 外部播放器

    init {
        randomTitle()
        loadAdData()
    }


    fun loadRegionData() {
        val isBestRegionNow = prefs.getBoolean("is_best_region", false)
        if (region.size > 0 && isBestRegion == isBestRegionNow) {
            return
        }
        isBestRegion = isBestRegionNow
        // 加载分区列表
        Observable.just(readRegionJson())
                .map { Gson().fromJson(it, Home.RegionData::class.java) }
                .map { it.data }
                .doOnNext { it.forEachWithIndex(::regionIcon) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    region.clear()
                    region.addAll(it)
                }, {
                    context.toast("读取分区列表遇到错误")
                })

        // 从网络读取最新版本的分区列表
        if (!isBestRegion) {
            MiaoHttp.getJson<ResultListInfo<Home.Region>>(BiliApiService.getRegion())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (it.code == 0) {
                            val regionList = it.data.filter { it.children != null && it.children.isNotEmpty() }
                            region.clear()
                            region.addAll(regionList)
                            // 保存到本地
                            writeRegionJson(
                                    Home.RegionData(
                                            data = regionList
                                    )
                            )
                        } else {
                            context.toast(it.msg)
                        }
                    }, {
                        context.toast("读取分区列表遇到错误")
                    })
        }
    }



    /**
     * 加载广告信息
     */
    private fun loadAdData() {
        val manager = context.packageManager
        val info = manager.getPackageInfo(context.packageName, 0)
        val longVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            info.versionCode.toLong()
        }
        val url = "https://bilimiao.10miaomiao.cn/miao/init?v=$longVersionCode"
        MiaoHttp.getJson<MiaoAdInfo>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ r ->
                    if (r.code == 0) {
                        adInfo set r.data.ad
                        showUpdateDialog(r.data.version, longVersionCode)
                    }
                }, { e ->
                    e.printStackTrace()
                })
    }


    fun showUpdateDialog(version: MiaoAdInfo.VersionBean, curVersionCode: Long) {
        // 当前版本大于等于最新版本不提示更新
        if (curVersionCode >= version.versionCode) {
            return
        }
        // 最新版已记录为不更新版本并且当前大于等于最低版本，不提示更新
        val noUpdateVersionCode = SettingUtil.getLong(context,"no_update_version_code", 0L)
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
                    SettingUtil.putLong(context,"no_update_version_code", version.versionCode)
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
     * 广告跳转
     */
    fun openAd() = adInfo.value?.let {
        //普通链接 调用浏览器
        var intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(it.link.url)
        context.startActivity(intent)
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

    private fun writeRegionJson(region: Home.RegionData) {
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
     * 随机标题
     */
    private fun randomTitle() {
        val titles = arrayOf("时光姬", "时光基", "时光姬", "时光姬")
        val subtitles = arrayOf("ε=ε=ε=┏(゜ロ゜;)┛", "(　o=^•ェ•)o　┏━┓", "(/▽＼)", "ヽ(✿ﾟ▽ﾟ)ノ")
        val random = Random()
        title set titles[random.nextInt(titles.size)] + "  " + subtitles[random.nextInt(titles.size)]
    }

    /**
     * 分区图标
     */
    private fun regionIcon(index: Int, item: Home.Region) {
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
