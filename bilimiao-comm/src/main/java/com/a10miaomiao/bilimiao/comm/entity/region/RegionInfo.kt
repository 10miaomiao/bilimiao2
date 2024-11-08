package com.a10miaomiao.bilimiao.comm.entity.region

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 分区信息
 */
@Parcelize
data class RegionInfo(
    var tid: Int,
    var reid: Int,
    var icon: Int?,
    var logo: String?,
    var name: String,
    var uri: String? = null,
    var type: Int,
    var children: List<RegionChildrenInfo>
) : Parcelable