package com.a10miaomiao.bilimiao.comm.entity.player

import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.createVideoPlayerSource

fun PlayListItemInfo.toVideoPlayerSource(): VideoPlayerSource {
    return createVideoPlayerSource(
        mainTitle = title,
        title = title,
        coverUrl = cover,
        aid = aid,
        id = cid,
        ownerId = ownerId,
        ownerName = ownerName,
    ).apply {
        pages = videoPages.map {
            VideoPlayerSource.PageInfo(
                cid = it.cid,
                title = it.part,
            )
        }
    }
}
