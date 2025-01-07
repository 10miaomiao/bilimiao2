package com.a10miaomiao.bilimiao.comm.entity.history

import kotlinx.serialization.Serializable

@Serializable
data class ToViewItemInfo(
    var aid: Long,
    var bvid: String,
    var cid: Long,
    var owner: OwnerInfo,
    val title: String,
    val pic: String,
    val videos: Int,
    val progress: Int,
    val pubdate: Long,
    val duration: Int,
    val left_icon_type: Int,
    val left_text: String,
    val right_icon_type: Int,
    val right_text: String,
) {
    @Serializable
    data class OwnerInfo(
        val name: String
    )
    // @Serializable
    // data class HistoryInfo(
    //     val oid: Long,
    //     val epid: Int,
    //     val bvid: String,
    //     val page: Int,
    //     val cid: Long,
    //     val part: String,
    //     val business: String,
    //     val dt: Int
    // )
}