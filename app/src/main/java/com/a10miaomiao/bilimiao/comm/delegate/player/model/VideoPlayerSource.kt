package com.a10miaomiao.bilimiao.comm.delegate.player.model

import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.SubtitleSourceInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.player.PlayerV2Info
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.CompressionTools
import com.a10miaomiao.bilimiao.widget.player.BiliDanmukuParser
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import java.io.ByteArrayInputStream
import java.io.InputStream

class VideoPlayerSource(
    override val title: String,
    override val coverUrl: String,
    var aid: String, // av号
    override var id: String, // cid
    override val ownerId: String,
    override val ownerName: String,
): BasePlayerSource() {

    override suspend fun getPlayerUrl(quality: Int, fnval: Int): PlayerSourceInfo {
//        val req = Playurl.PlayURLReq.newBuilder()
//            .setAid(aid.toLong())
//            .setCid(cid.toLong())
//            .setQn(quality.toLong())
//            .setDownload(0)
//            .setForceHost(2)
//            .setFourk(false)
//            .build()
//        val result = PlayURLGrpc.getPlayURLMethod()
//            .request(req)
//            .awaitCall()
//        return getMpdUrl(quality, result.dash)
        val res = BiliApiService.playerAPI
            .getVideoPalyUrl(aid, id, quality, fnval)

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
        val res = BiliApiService.playerAPI.getDanmakuList(id)
            .awaitCall()
        val body = res.body
        return if (body == null) {
            null
        } else {
            ByteArrayInputStream(CompressionTools.decompressXML(body.bytes()))
        }
    }

    override suspend fun getSubtitles(): List<SubtitleSourceInfo> {
        try {
            val res = BiliApiService.playerAPI
                .getPlayerV2Info(aid = aid, cid = id)
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

    override suspend fun historyReport(progress: Long) {
        try {
            val realtimeProgress = progress.toString()  // 秒数
            MiaoHttp.request {
                url = "https://api.bilibili.com/x/v2/history/report"
                formBody = ApiHelper.createParams(
                    "aid" to aid,
                    "cid" to id,
                    "progress" to realtimeProgress,
                    "realtime" to realtimeProgress,
                    "type" to "3"
                )
                method = MiaoHttp.POST
            }.awaitCall()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}