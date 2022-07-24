package com.a10miaomiao.bilimiao.comm.delegate.player.model

import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.utils.CompressionTools
import com.a10miaomiao.bilimiao.widget.player.BiliDanmukuParser
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import java.io.ByteArrayInputStream
import java.io.InputStream

class BangumiPlayerSource(
    val sid: String,
    val epid: String,
    val aid: String,
    val cid: String,
    override val title: String,
    override val coverUrl: String,
): BasePlayerSource {

    override suspend fun getPlayerUrl(quality: Int): PlayerSourceInfo {
        val res = BiliApiService.playerAPI
            .getBangumiUrl(epid, cid, quality, dash = true)
        val dash = res.dash
        val url = if (dash != null) {
            DashSource(quality, dash).getMDPUrl()
        } else {
            res.durl!![0].url
        }
        val acceptDescription = res.accept_description
        val acceptList = res.accept_quality.mapIndexed { index, i ->
            PlayerSourceInfo.AcceptInfo(i, acceptDescription[index])
        }
        return PlayerSourceInfo(url, res.quality, acceptList)
    }

    override suspend fun getDanmakuParser(): BaseDanmakuParser? {
        val inputStream = getBiliDanmukuStream()
        return if (inputStream == null) {
            null
        } else {
            val loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)
            loader.load(inputStream)
            val parser = BiliDanmukuParser()
            val dataSource = loader.dataSource
            parser.load(dataSource)
            parser
        }
    }

    private suspend fun getBiliDanmukuStream(): InputStream? {
        val res = BiliApiService.playerAPI.getDanmakuList(cid)
            .awaitCall()
        val body = res.body()
        return if (body == null) {
            null
        } else {
            ByteArrayInputStream(CompressionTools.decompressXML(body.bytes()))
        }
    }

    override suspend fun historyReport(progress: Long) {
        try {
            val realtimeProgress = progress.toString()  // 秒数
            MiaoHttp.request {
                url = "https://api.bilibili.com/x/v2/history/report"
                formBody = ApiHelper.createParams(
                    "aid" to aid,
                    "cid" to cid,
                    "epid" to cid,
                    "sid" to cid,
                    "progress" to realtimeProgress,
                    "realtime" to realtimeProgress,
                    "type" to "4",
                    "sub_type" to "1",
                )
                method = MiaoHttp.POST
            }.awaitCall()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}