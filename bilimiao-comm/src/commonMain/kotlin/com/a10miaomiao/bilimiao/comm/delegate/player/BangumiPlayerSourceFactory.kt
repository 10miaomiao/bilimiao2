package com.a10miaomiao.bilimiao.comm.delegate.player

data class BangumiEpisodeInfo(
    val epid: String,
    val aid: String,
    val cid: String,
    val cover: String,
    val index: String,
    val index_title: String,
    val badge: String,
    val badge_info: BangumiEpisodeBadgeInfo,
)

data class BangumiEpisodeBadgeInfo(
    val bg_color: String,
    val bg_color_night: String,
    val text: String,
)

expect fun createBangumiPlayerSource(
    sid: String,
    epid: String,
    aid: String,
    id: String,
    title: String,
    coverUrl: String,
    ownerId: String,
    ownerName: String,
    episodes: List<BangumiEpisodeInfo>,
): BasePlayerSource
