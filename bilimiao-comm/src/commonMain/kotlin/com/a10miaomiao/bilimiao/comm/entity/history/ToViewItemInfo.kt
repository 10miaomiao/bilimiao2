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
    val page: PageInfo? = null,
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
        val mid: String,
        val name: String,
    )

     @Serializable
     data class PageInfo(
         val cid: String,
         val part: String,
         val page: Int,
         val duration: Int,
     )
}