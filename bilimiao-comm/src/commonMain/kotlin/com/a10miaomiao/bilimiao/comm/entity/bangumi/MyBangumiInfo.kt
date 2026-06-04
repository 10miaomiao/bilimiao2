package com.a10miaomiao.bilimiao.comm.entity.bangumi

import kotlinx.serialization.Serializable

@Serializable
data class MyBangumiInfo(
    val badge: String,
    val badge_info: BangumiBadgeInfo? = null,
    val badge_type: Int,
    val can_watch: Int,
    val cover: String,
    val follow: Int,
    val is_finish: Int,
    val movable: Int,
    val mtime: Int,
    val new_ep: MyBangumiNewEpInfo,
    val progress: MyBangumiProgressInfo? = null,
    val season_id: String,
    val season_type: Int,
    val season_type_name: String,
    val series: SeriesInfo,
    val square_cover: String,
    val title: String,
    val url: String
)