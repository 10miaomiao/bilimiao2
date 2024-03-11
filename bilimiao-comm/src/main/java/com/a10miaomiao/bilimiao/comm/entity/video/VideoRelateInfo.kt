package com.a10miaomiao.bilimiao.comm.entity.video

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoRelateInfo (
    // 0/1/2
    val ad_index: Int,
    // av format avid
    val aid: String?,
    // 0
    val card_index: Int,
    val cid: Double,
//    val client_ip: String,
    // seconds
    val duration: Long,
    // av
    val goto: String,
    val is_ad_loc: Boolean,
    val owner: VideoOwnerInfo?,
    val `param`: String,
    val pic: String,
//    val request_id: String,
    val src_id: Int,
    val stat: VideoStatInfo?,
    val title: String,
    val trackid: String,
    val uri: String,
) : Parcelable