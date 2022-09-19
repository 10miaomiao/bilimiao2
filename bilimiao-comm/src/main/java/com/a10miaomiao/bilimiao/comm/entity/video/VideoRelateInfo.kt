package com.a10miaomiao.bilimiao.comm.entity.video

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VideoRelateInfo (
    val ad_index: Int,
    val aid: String?,
    val card_index: Int,
    val cid: Double,
    val client_ip: String,
    val duration: Int,
    val goto: String,
    val is_ad_loc: Boolean,
    val owner: VideoOwnerInfo?,
    val `param`: String,
    val pic: String,
    val request_id: String,
    val src_id: Int,
    val stat: VideoStatInfo?,
    val title: String,
    val trackid: String,
    val uri: String,
) : Parcelable