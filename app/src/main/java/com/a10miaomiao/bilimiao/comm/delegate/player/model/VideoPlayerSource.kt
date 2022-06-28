package com.a10miaomiao.bilimiao.comm.delegate.player.model

import bilibili.app.playurl.v1.PlayURLGrpc
import bilibili.app.playurl.v1.Playurl
import com.a10miaomiao.bilimiao.comm.apis.PlayerAPI
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.request
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao

class VideoPlayerSource(
    override val title: String,
    var aid: String,
    var cid: String,
): BasePlayerSource {

    override suspend fun getPlayerUrl(quality: Int): String {
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
            .getVideoPalyUrl(aid, cid, quality, dash = true)
        val dash = res.dash
        if (dash != null) {
            return DashSource(quality, dash).getMDPUrl()
        }
        val durl = res.durl
        return durl!![0].url
    }

}