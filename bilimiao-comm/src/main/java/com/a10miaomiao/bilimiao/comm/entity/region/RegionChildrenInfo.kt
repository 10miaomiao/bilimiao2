package com.a10miaomiao.bilimiao.comm.entity.region

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

/**
 * 子分区信息
 */
@Parcelize
@Serializable
data class RegionChildrenInfo(
    var tid: Int,
    var reid: Int,
    var name: String,
    var type: Int = 0
) : Parcelable