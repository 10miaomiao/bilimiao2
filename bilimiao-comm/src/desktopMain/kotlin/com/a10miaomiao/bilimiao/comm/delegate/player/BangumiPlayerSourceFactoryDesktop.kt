package com.a10miaomiao.bilimiao.comm.delegate.player

import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceIds
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo

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
    return BangumiPlayerSourceDesktop(
        sid = sid,
        epid = epid,
        aid = aid,
        id = id,
        title = title,
        coverUrl = coverUrl,
        ownerId = ownerId,
        ownerName = ownerName,
    )
}

private class BangumiPlayerSourceDesktop(
    val sid: String,
    val epid: String,
    val aid: String,
    override val id: String,
    override val title: String,
    override val coverUrl: String,
    override val ownerId: String,
    override val ownerName: String,
) : BasePlayerSource() {
    override suspend fun getPlayerUrl(quality: Int, fnval: Int): PlayerSourceInfo {
        return defaultPlayerSource
    }

    override fun getSourceIds(): PlayerSourceIds {
        return PlayerSourceIds(
            cid = id,
            sid = sid,
            epid = epid,
            aid = aid,
        )
    }
}
