package com.a10miaomiao.bilimiao.comm.entity.region

import kotlinx.serialization.Serializable

@Serializable
data class RegionVideoInfo(
    val author: String,
    val bvid: String,
    val description: String,
    val duration: String,
    val favorites: String,
    val id: String,
    val is_pay: Int,
    val is_union_video: Int,
    val mid: String,
    val pic: String,
    val play: String,
    val pubdate: String,
    val rank_index: Int,
    val rank_offset: Int,
    val rank_score: Int,
    val review: String,
    val senddate: Long,
    val tag: String,
    val title: String,
    val type: String,
    val video_review: String,
)