package com.a10miaomiao.bilimiao.comm.entity.media

import kotlinx.serialization.Serializable

@Serializable
data class MediasInfo(
    val id: String,
    val cover: String,
    val ctime: Long,
    val duration: Long,
    val title: String,
    val upper: MediaUpperInfo,
    val cnt_info: CntInfo,
    val ugc: Ugc? = null,
    val ogv: Ogv? = null,
) {

    @Serializable
    data class CntInfo(
        val coin: Int = 0,
        val collect: Int = 0,
        val danmaku: String,
        val play: String,
        val reply: Int = 0,
        val share: Int = 0,
        val thumb_down: Int = 0,
        val thumb_up: Int = 0
    )
    @Serializable
    data class Ugc(
        val first_cid: String,
    )
    @Serializable
    data class Ogv(
        val season_id: String,
    )
}