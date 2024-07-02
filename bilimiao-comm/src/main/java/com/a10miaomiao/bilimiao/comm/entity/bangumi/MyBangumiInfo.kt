package com.a10miaomiao.bilimiao.comm.entity.bangumi


data class MyBangumiInfo(
    val badge: String,
    val badge_info: BangumiBadgeInfo?,
    val badge_type: Int,
    val can_watch: Int,
    val cover: String,
    val follow: Int,
    val is_finish: Int,
    val movable: Int,
    val mtime: Int,
    val new_ep: MyBangumiNewEpInfo,
    val progress: MyBangumiProgressInfo?,
    val season_id: String,
    val season_type: Int,
    val season_type_name: String,
    val series: SeriesInfo,
    val square_cover: String,
    val title: String,
    val url: String
)