package com.a10miaomiao.bilimiao.netword

import android.net.Uri
import android.os.SystemClock
import cn.a10miaomiao.player.VideoSource
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.utils.DebugMiao
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.util.HashMap

object PlayurlHelper {

    private val _appKey_VIDEO = "84956560bc028eb7"
    private val _appSecret_VIDEO = "94aba54af9065f71de72f5508f1cd42e"

    private fun getVideoHeaders(avid: String) = mapOf(
            "Referer" to "https://www.bilibili.com/av$avid",
            "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36"
    )

    /**
     * 获取视频播放地址
     */
    fun getVideoPalyUrl(avid: String, cid: String, quality: Int = 64) = Observable.create<PlayurlData> {
        val params = mutableMapOf(
                "avid" to avid,
                "cid" to cid,
                "qn" to quality.toString(),
                "type" to "",
                "otype" to "json",
                "appkey" to _appKey_VIDEO
        )
        ApiHelper.addAccessKeyAndMidToParams(params)
        params["sign"] = ApiHelper.getSing(params, _appSecret_VIDEO)
        var url = "https://api.bilibili.com/x/player/playurl?" + ApiHelper.urlencode(params)
        MiaoHttp.getJson<ResultInfo<PlayurlData>>(url) {
            headers = getVideoHeaders(avid)
        }.subscribe({ r ->
            if (r.code == 0) {
                it.onNext(r.data)
                it.onComplete()
            } else {
                it.onError(Throwable(r.message))
            }
            DebugMiao.log(r)
        }, { e ->
            e.printStackTrace()
            it.onError(Throwable("读取播放地址失败"))
        })
    }

    /**
     * 获取番剧播放地址
     */
    fun getBangumiUrl(epid: String, cid: String, qn: Int = 64) = Observable.create<PlayurlData> {
        val params = mutableMapOf(
                "aid" to epid,
                "cid" to cid,
                "fnval" to "2",
                "fnver" to "0",
                "module" to "bangumi",
                "qn" to qn.toString(),
                "season_type" to "1",
                "session" to ApiHelper.getMD5((System.currentTimeMillis() - SystemClock.currentThreadTimeMillis()).toString()),
                "track_path" to "",
                "appkey" to ApiHelper.APP_KEY_NEW,
                "device" to "android",
                "mobi_app" to "android",
                "platform" to "android"
        )
        ApiHelper.addAccessKeyAndMidToParams(params)
        params["sign"] = ApiHelper.getSing(params, ApiHelper.APP_SECRET_NEW)
        var url = "https://api.bilibili.com/pgc/player/api/playurl?" + ApiHelper.urlencode(params)
        MiaoHttp.getJson<PlayurlData>(url)
                .subscribe({ r ->
                    if (r.code == 0) {
                        it.onNext(r)
                        it.onComplete()
                    } else {
                        it.onError(Throwable(r.message))
                    }
                    DebugMiao.log(r)
                }, { e ->
                    e.printStackTrace()
                    it.onError(Throwable("读取播放地址失败"))
                })
    }

    data class PlayurlData(
            val accept_description: List<String>,
            val accept_format: String,
            val accept_quality: List<Int>,
            val format: String,
            val from: String,
            val message: String,
            val quality: Int,
            val result: String,
            val seek_param: String,
            val seek_type: String,
            val timelength: Int,
            val video_codecid: Int,
            val durl: List<Durl>,
            val code: Int
    )

    data class Durl(
            val ahead: String,
            val length: Long,
            val order: Int,
            val size: Long,
            val url: String,
            val vhead: String
    )
}