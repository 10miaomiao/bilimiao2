package com.a10miaomiao.bilimiao.comm.delegate.player.model

import com.a10miaomiao.bilimiao.comm.network.BiliApiService

class BangumiPlayerSource(
    val sid: String,
    val epid: String,
    val cid: String,
    override val title: String,
): BasePlayerSource {

    override suspend fun getPlayerUrl(quality: Int): String {
        val res = BiliApiService.playerAPI
            .getBangumiUrl(epid, cid, quality, dash = true)
        val dash = res.dash
        if (dash != null) {
            return DashSource(quality, dash).getMDPUrl()
        }
        val durl = res.durl
        return durl!![0].url
    }

}