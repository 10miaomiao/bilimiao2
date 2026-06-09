package com.a10miaomiao.bilimiao.comm.delegate.player

actual fun createBangumiPlayerSource(
    sid: String,
    epid: String,
    aid: String,
    id: String,
    title: String,
    coverUrl: String,
    ownerId: String,
    ownerName: String,
    episodes: List<BangumiEpisodeInfo>,
): BasePlayerSource {
    val source = BangumiPlayerSource(
        sid = sid,
        epid = epid,
        aid = aid,
        id = id,
        title = title,
        coverUrl = coverUrl,
        ownerId = ownerId,
        ownerName = ownerName,
    )
    source.episodes = episodes.map {
        BangumiPlayerSource.EpisodeInfo(
            epid = it.epid,
            aid = it.aid,
            cid = it.cid,
            cover = it.cover,
            index = it.index,
            index_title = it.index_title,
            badge = it.badge,
            badge_info = BangumiPlayerSource.EpisodeBadgeInfo(
                text = it.badge_info.text,
                bg_color = it.badge_info.bg_color,
                bg_color_night = it.badge_info.bg_color_night,
            ),
        )
    }
    return source
}
