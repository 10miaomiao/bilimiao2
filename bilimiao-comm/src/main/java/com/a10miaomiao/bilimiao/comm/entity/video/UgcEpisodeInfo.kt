package com.a10miaomiao.bilimiao.comm.entity.video

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UgcEpisodeInfo(
    val aid: String,
    val author: VideoOwnerInfo,
    val author_desc: String,
    val bvid: String,
    val cid: String,
    val cover: String,
    val cover_right_text: String,
//    val dimension: Dimension,
    val duration: Int,
    val first_frame: String,
    val from: String,
    val id: Int,
//    val metas: List<Meta>,
    val page: Int,
    val part: String,
    val stat: VideoStatInfo,
    val stat_vt_display: String,
    val title: String,
    val vid: String,
    val weblink: String
): Parcelable

data class Author(
    val face: String,
    val mid: Int,
    val name: String
)

data class Dimension(
    val height: Int,
    val rotate: Int,
    val width: Int
)

data class Meta(
    val format: String,
    val quality: Int,
    val size: Int
)

data class Stat(
    val aid: Int,
    val coin: Int,
    val danmaku: Int,
    val dislike: Int,
    val favorite: Int,
    val his_rank: Int,
    val like: Int,
    val now_rank: Int,
    val reply: Int,
    val share: Int,
    val view: Int,
    val vt: Int,
    val vv: Int
)