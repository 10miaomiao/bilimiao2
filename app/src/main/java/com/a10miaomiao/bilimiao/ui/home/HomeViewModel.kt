package com.a10miaomiao.bilimiao.ui.home

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.entity.Home
import com.a10miaomiao.bilimiao.entity.MiaoAdInfo
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.toast
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import android.os.Build


class HomeViewModel(
        val context: Context
) : ViewModel() {

    var title = MiaoLiveData("时光姬")
    var adInfo = MiaoLiveData<MiaoAdInfo.DataBean?>(null)
    var region = MiaoList<Home.Region>()

    init {
        loadRegionData()
        randomTitle()
        loadAdData()
    }


    private fun loadRegionData() {
        // 加载分区列表
        Observable.just(readAssetsJson())
                .map { Gson().fromJson(it, Home.RegionData::class.java) }
                .map { it.data }
                .doOnNext { it.forEachWithIndex(::regionIcon) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    region.addAll(it)
                }, {
                    context.toast("读取分区列表遇到错误")
                })
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
        val url = "https://10miaomiao.cn/miao/bilimiao/ad?v=$longVersionCode"
        MiaoHttp.getJson<MiaoAdInfo>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ r ->
                    if (r.code == 0) {
                        adInfo set r.data
                    }
                }, { e ->

                })
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
    private fun readAssetsJson(): String? {
        val assetManager = context.assets
        try {
            val inputStream = assetManager.open("region.json")
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
        item.icon = intArrayOf(
                R.drawable.ic_region_fj, R.drawable.ic_region_fj_domestic, R.drawable.ic_region_dh,
                R.drawable.ic_region_yy, R.drawable.ic_region_wd, R.drawable.ic_region_yx,
                R.drawable.ic_region_kj, R.drawable.ic_region_sh, R.drawable.ic_region_gc,
                R.drawable.ic_region_ss, R.drawable.ic_region_ad, R.drawable.ic_region_yl,
                R.drawable.ic_region_ys, R.drawable.ic_region_dy, R.drawable.ic_region_dsj
        )[index]
//        item.icon = intArrayOf(
//                R.drawable.ic_category_t13, R.drawable.ic_category_t167, R.drawable.ic_category_t1,
//                R.drawable.ic_category_t3, R.drawable.ic_category_t129, R.drawable.ic_category_t4,
//                R.drawable.ic_category_t36, R.drawable.ic_category_t160, R.drawable.ic_category_t119,
//                R.drawable.ic_category_t155, R.drawable.ic_category_t165, R.drawable.ic_category_t5,
//                R.drawable.ic_category_t181, R.drawable.ic_category_t23, R.drawable.ic_category_t11
//        )[index]
    }

}
