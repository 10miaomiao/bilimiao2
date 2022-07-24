package com.a10miaomiao.bilimiao.comm.delegate.player.model

import com.a10miaomiao.bilimiao.comm.apis.PlayerAPI
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser

interface BasePlayerSource {
    val title: String
    val coverUrl: String
    suspend fun getPlayerUrl(quality: Int): PlayerSourceInfo
    suspend fun getDanmakuParser(): BaseDanmakuParser? = null
    suspend fun historyReport(progress: Long) {}
}