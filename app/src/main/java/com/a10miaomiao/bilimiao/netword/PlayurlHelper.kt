package com.a10miaomiao.bilimiao.netword

import android.net.Uri
import cn.a10miaomiao.player.VideoSource
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.utils.DebugMiao
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

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
    fun getVideoPalyUrl(avid: String, cid: String, quality: Int = 0) = Observable.create<List<VideoSource>> {
        val sources = ArrayList<VideoSource>()
        var url = "https://api.bilibili.com/x/player/playurl?avid=$avid&cid=$cid&qn=$quality&type=&otype=json&appkey=$_appKey_VIDEO"
        url += "&sign=" + ApiHelper.getSing(url, _appSecret_VIDEO)
        MiaoHttp.getJson<ResultInfo<PlayurlData>>(url) {
            headers = getVideoHeaders(avid)
        }.subscribe({ r ->
            if (r.code == 0) {
                for (durl in r.data.durl) {
                    sources += VideoSource().apply {
                        uri = Uri.parse(durl.url)
                        length = durl.length
                        size = durl.size
                    }
                }
                it.onNext(sources)
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
    fun getBangumiUrl(cid: String, qn: Int = 64) = Observable.create<List<VideoSource>> {
        // https://bangumi.bilibili.com/player/web_api/v2/playurl?cid={1}&appkey={0}&otype=json&type=&quality={2}&module=bangumi&season_type=1&qn={2}&ts={3}", ApiHelper._appKey_VIDEO, model.Mid, qn, ApiHelper.GetTimeSpan_2)
        val sources = ArrayList<VideoSource>()
        var url = "https://bangumi.bilibili.com/player/web_api/v2/playurl?cid=$cid&appkey=$_appKey_VIDEO&otype=json&type=&quality=$qn&module=bangumi&season_type=1&qn=$qn&ts=${ApiHelper.getTimeSpen()}"
        url += "&sign=" + ApiHelper.getSing(url, _appSecret_VIDEO)
        MiaoHttp.getJson<PlayurlData>(url)
                .subscribe({ r ->
                    if (r.code == 0) {
                        for (durl in r.durl) {
                            sources += VideoSource().apply {
                                uri = Uri.parse(durl.url)
                                length = durl.length
                                size = durl.size
                            }
                        }
                        it.onNext(sources)
                        it.onComplete()
                    } else {
                        it.onError(Throwable("读取播放地址失败"))
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