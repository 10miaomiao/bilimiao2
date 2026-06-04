package com.a10miaomiao.bilimiao.comm.entity.player

import kotlinx.serialization.Serializable

@Serializable
data class PlayListItemInfo(
    val aid: String,
    val cid: String,
    val duration: Int,
    val title: String,
    val cover: String,
    val ownerId: String,
    val ownerName: String,
    val from: PlayListFrom,
    val videoPages: List<VideoPageInfo> = listOf(),
) {
    @Serializable
    data class VideoPageInfo(
        val cid: String,
        val page: Int,
        val part: String,
        val duration: Int,
    )
}
