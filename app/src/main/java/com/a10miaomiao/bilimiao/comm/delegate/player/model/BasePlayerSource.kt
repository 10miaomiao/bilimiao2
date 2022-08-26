package com.a10miaomiao.bilimiao.comm.delegate.player.model

import com.a10miaomiao.bilimiao.comm.apis.PlayerAPI
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser

interface BasePlayerSource {
    val id: String // cid
    val title: String
    val coverUrl: String
    val ownerId: String
    val ownerName: String
    suspend fun getPlayerUrl(quality: Int): PlayerSourceInfo
    suspend fun getDanmakuParser(): BaseDanmakuParser? = null
    suspend fun historyReport(progress: Long) {}
}