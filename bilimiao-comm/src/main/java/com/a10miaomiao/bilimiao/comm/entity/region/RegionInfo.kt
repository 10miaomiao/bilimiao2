package com.a10miaomiao.bilimiao.comm.entity.region

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

/**
 * 分区信息
 */
@Parcelize
@Serializable
data class RegionInfo(
    var tid: Int,
    var reid: Int,
    var icon: Int? = null,
    var logo: String? = null,
    var name: String,
    var uri: String? = null,
    var type: Int,
    var children: List<RegionChildrenInfo>? = null
) : Parcelable