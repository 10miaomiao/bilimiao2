package com.a10miaomiao.bilimiao.comm.entity.video

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VideoStatInfo(
    val aid: String,
    var coin: Int,
    val danmaku: String,
    val dislike: Int,
    var favorite: Int,
    val his_rank: Int,
    var like: Int,
    val now_rank: Int,
    val reply: Int,
    val share: Int,
    val view: String
) : Parcelable {
    constructor(danmaku: String, view: String) : this(
        "", 0, danmaku,
        0, 0, 0, 0,
        0, 0, 0, view
    )
}