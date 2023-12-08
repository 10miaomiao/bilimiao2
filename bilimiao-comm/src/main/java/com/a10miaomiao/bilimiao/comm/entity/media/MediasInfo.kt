package com.a10miaomiao.bilimiao.comm.entity.media

data class MediasInfo(
    val id: String,
    val cover: String,
    val ctime: Long,
    val duration: Long,
    val title: String,
    val upper: UpperInfo,
    val cnt_info: CntInfo
) {
    data class UpperInfo(
        val face: String,
        val name: String,
        val mid: String
    )

    data class CntInfo(
        val coin: Int,
        val collect: Int,
        val danmaku: String,
        val play: String,
        val reply: Int,
        val share: Int,
        val thumb_down: Int,
        val thumb_up: Int
    )
}