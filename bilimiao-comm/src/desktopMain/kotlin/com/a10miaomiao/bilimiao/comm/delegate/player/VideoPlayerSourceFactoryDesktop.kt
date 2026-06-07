package com.a10miaomiao.bilimiao.comm.delegate.player

actual fun createVideoPlayerSource(
    mainTitle: String,
    title: String,
    coverUrl: String,
    aid: String,
    id: String,
    ownerId: String,
    ownerName: String,
): VideoPlayerSource {
    return VideoPlayerSource(mainTitle, title, coverUrl, aid, id, ownerId, ownerName)
}
