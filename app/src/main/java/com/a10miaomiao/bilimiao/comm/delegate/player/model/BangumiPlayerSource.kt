package com.a10miaomiao.bilimiao.comm.delegate.player.model

import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.SubtitleSourceInfo
import com.a10miaomiao.bilimiao.comm.exception.DabianException
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.player.PlayerV2Info
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import com.a10miaomiao.bilimiao.comm.utils.CompressionTools
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.widget.player.BiliDanmukuParser
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import java.io.ByteArrayInputStream
import java.io.InputStream

class BangumiPlayerSource(
    val sid: String,
    val epid: String,
    val aid: String,
    override val id: String,
    override val title: String,
    override val coverUrl: String,
    override val ownerId: String,
    override val ownerName: String,
): BasePlayerSource() {

    override suspend fun getPlayerUrl(quality: Int, fnval: Int): PlayerSourceInfo {
        val res = proxyServer?.let {
            BiliApiService.playerAPI.getBangumiUrl(
                epid, id, quality, fnval,
                noToken = !it.isTrust,
                proxyHost = it.host
            )
        } ?: BiliApiService.playerAPI.getBangumiUrl(
            epid, id, quality, fnval
        )
        return PlayerSourceInfo().also {
            it.lastPlayCid = res.last_play_cid
            it.lastPlayTime = res.last_play_time
            it.quality = res.quality
            it.acceptList = res.accept_quality.mapIndexed { index, i ->
                PlayerSourceInfo.AcceptInfo(i, res.accept_description[index])
            }
            val dash = res.dash
            if (dash != null) {
                it.duration = dash.duration * 1000L
                val dashSource = DashSource(res.quality, dash)
                val dashVideo = dashSource.getDashVideo()!!
                it.height = dashVideo.height
                it.width = dashVideo.width
                it.url = dashSource.getMDPUrl(dashVideo)
            } else {
                val durl = res.durl!!
                if (durl.size == 1) {
                    it.duration = durl[0].length * 1000L
                    it.url = durl[0].url
                } else {
                    var duration = 0L
                    it.url = "[concatenating]\n" + durl.joinToString("\n") { d ->
                        duration += d.length * 1000L
                        d.url
                    }
                    it.duration = duration
                }

            }
        }
    }

    override suspend fun getSubtitles(): List<SubtitleSourceInfo> {
        try {
            val res = BiliApiService.playerAPI
                .getPlayerV2Info(aid = aid, cid = id, epId = epid, seasonId = sid)
                .awaitCall()
                .gson<ResultInfo<PlayerV2Info>>()
            if (res.isSuccess) {
                return res.data.subtitle.subtitles.map {
                    SubtitleSourceInfo(
                        id = it.id,
                        lan = it.lan,
                        lan_doc = it.lan_doc,
                        subtitle_url = it.subtitle_url,
                        ai_status = it.ai_status,
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
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
        if (sid == "26257") {
            // 答辩就不要看了
            throw DabianException()
        }
        val res = BiliApiService.playerAPI.getDanmakuList(id)
            .awaitCall()
        val body = res.body
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
                    "cid" to id,
                    "epid" to epid,
                    "sid" to sid,
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