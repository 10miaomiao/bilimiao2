package com.a10miaomiao.bilimiao.comm.entity.bangumi

import android.os.Parcelable
import com.a10miaomiao.bilimiao.comm.entity.comm.bili.BadgeInfo
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class EpisodeInfo(
    val aid: String,
    val badge: String,
    val badge_type: Int,
    val badge_info: BadgeInfo,
    val cid: String,
    val cover: String,
    val dimension: DimensionXInfo? = null,
    val from: String,
    val id: String,
    val ep_id: String = "",
    val index: String = "",
    val index_title: String = "",
    val long_title: String = "",
//    val pub_real_time: String,
//    val section_id: String,
//    val section_type: Int,
    val share_url: String,
    val status: Int,
    val title: String,
    val vid: String
) : Parcelable
