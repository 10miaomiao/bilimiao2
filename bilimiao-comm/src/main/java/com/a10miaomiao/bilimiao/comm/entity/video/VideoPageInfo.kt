package com.a10miaomiao.bilimiao.comm.entity.video

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class VideoPageInfo(
    val cid: String,
//    val dimension: Dimension,
//    val dm: Dm,
    val dmlink: String,
    val duration: Int,
    val from: String,
//    val metas: List<Meta>,
    val page: Int,
    var part: String,
    val vid: String,
    val weblink: String
) : Parcelable