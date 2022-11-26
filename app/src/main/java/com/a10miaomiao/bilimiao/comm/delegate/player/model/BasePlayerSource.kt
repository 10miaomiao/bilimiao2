package com.a10miaomiao.bilimiao.comm.delegate.player.model

import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSubtitleSourceInfo
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser

interface BasePlayerSource {
    val id: String // cid
    val title: String
    val coverUrl: String
    val ownerId: String
    val ownerName: String
    suspend fun getPlayerUrl(quality: Int, fnval: Int): PlayerSourceInfo
    suspend fun getSubtitles(): List<DanmakuVideoPlayer.SubtitleSourceInfo> = emptyList()
    suspend fun getDanmakuParser(): BaseDanmakuParser? = null
    suspend fun historyReport(progress: Long) {}
}