package com.a10miaomiao.bilimiao.comm.entity.player

import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource

fun PlayListItemInfo.toVideoPlayerSource(): VideoPlayerSource {
    return VideoPlayerSource(
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
