package com.a10miaomiao.bilimiao.comm.entity.player

import android.os.Parcelable
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlayListItemInfo(
    val aid: String,
    val cid: String,
    val duration: Int,
    val title: String,
    val cover: String,
    val ownerId: String,
    val ownerName: String,
    val from: PlayListFrom,
) : Parcelable {
    fun toVideoPlayerSource(): VideoPlayerSource {
        return VideoPlayerSource(
            mainTitle = title,
            title = title,
            coverUrl = cover,
            aid = aid,
            id = cid,
            ownerId = ownerId,
            ownerName = ownerName,
        )
    }
}