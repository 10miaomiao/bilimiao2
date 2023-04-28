package com.a10miaomiao.bilimiao.comm.apis

import android.os.SystemClock
import android.widget.Toast
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.exception.AreaLimitException
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil

class PlayerAPI {

    private fun getVideoHeaders(avid: String) = mapOf(
        "Referer" to "https://www.bilibili.com/av$avid",
        "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36"
    )


    fun getPlayerV2Info(
        aid: String,
        cid: String,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/player/v2",
            "aid" to aid,
            "cid" to cid,
        )
    }

    fun getPlayerV2Info(
        aid: String,
        cid: String,
        epId: String,
        seasonId: String,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/player/v2",
            "aid" to aid,
            "cid" to cid,
            "ep_id" to epId,
            "season_id" to seasonId,
        )
    }

    /**
     * 获取视频播放地址
     * fnval: 976:flv,1:mp4,4048:dash
     */
    fun getVideoPalyUrl(
        avid: String,
        cid: String,
        quality: Int = 64,
        fnval: Int = 4048,
    ): PlayurlData {
        val params = mutableMapOf<String, String?>(
            "avid" to avid,
            "cid" to cid,
            "qn" to quality.toString(),
            "fnval" to fnval.toString(),
            "fnver" to "0",
            "force_host" to "2", // 强制音视频返回 https
            "type" to "",
            "otype" to "json",
        )
        if (fnval > 2) {
            params.put("fourk", "1")
        }
        val res = MiaoHttp.request {
            url = BiliApiService.biliApi("x/player/playurl", *params.toList().toTypedArray())
            headers = getVideoHeaders(avid)
        }.call().gson<ResultInfo<PlayurlData>>()
        if (res.code == 0) {
            return res.data
        } else {
            throw Exception(res.message)
        }
    }

    /**
     * 获取番剧播放地址
     */
    suspend fun getBangumiUrl(
        epid: String,
        cid: String,
        qn: Int = 64,
        fnval: Int = 4048,
        noToken: Boolean = false,
        proxyHost: String? = null,
    ): PlayurlData {
        val params = mutableMapOf<String, String?>(
            "ep_id" to epid,
            "cid" to cid,
            "fnval" to fnval.toString(),
            "fnver" to "0",
            "force_host" to "2", // 强制音视频返回 https
            "module" to "bangumi",
            "qn" to qn.toString(),
            "season_type" to "1",
            "session" to ApiHelper.getMD5((System.currentTimeMillis() - SystemClock.currentThreadTimeMillis()).toString()),
            "track_path" to "",
            "device" to "android",
            "mobi_app" to "android",
            "platform" to "android"
        )
        if (fnval > 2) {
            params["fourk"] = "1"
        }
        if (noToken) {
            params["notoken"] = "1"
        }
        val res = MiaoHttp.request {
            url = BiliApiService.biliApi(
                "pgc/player/api/playurl",
                *params.toList().toTypedArray()
            ).let {
                if (proxyHost != null) {
                    UrlUtil.replaceHost(it, proxyHost)
                } else {
                    it
                }
            }

        }.awaitCall().gson<PlayurlData>()
        if (res.code == 0) {
            return res
        } else if (res.code == -10403) {
            throw AreaLimitException()
        } else {
            throw Exception(res.message)
        }
    }

    fun getDanmakuList(cid: String): MiaoHttp {
        return MiaoHttp.request {
            url = "https://comment.bilibili.com/$cid.xml"
        }
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
        // 时长，毫秒
        val timelength: Int,
        val video_codecid: Int,
        val durl: List<Durl>?,
        val dash: Dash?,
        val code: Int,
        val support_formats: List<SupportFormats>,
        val last_play_time: Long,
        val last_play_cid: String,
    )

    data class Durl(
        val ahead: String,
        val length: Long,
        val order: Int,
        val size: Long,
        val url: String,
        val vhead: String
    )

    data class SupportFormats(
        val quality: Int,
        val format: String,
        val new_description: String,
        val display_desc: String,
        val superscript: String
    )

    data class Dash (
        // 时长，秒
        val duration: Long,
        val min_buffer_time: Double,
        val video: List<DashItem>,
        val audio: List<DashItem>,
    )

    data class DashItem(
        val id: Int,
        val bandwidth: Int,
        val base_url: String,
        val backup_url: List<String>,
        val mime_type: String,
        val codecid: Int,
        val codecs: String,
        val width: Int,
        val height: Int,
        val frame_rate: String,
        val segment_base: SegmentBase,
    )

    data class SegmentBase(
        val initialization: String,
        val index_range: String,
    )

}