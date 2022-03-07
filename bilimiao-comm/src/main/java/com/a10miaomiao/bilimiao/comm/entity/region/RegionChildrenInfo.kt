package com.a10miaomiao.bilimiao.comm.entity.region

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 子分区信息
 */
@Parcelize
data class RegionChildrenInfo(
    var tid: Int,
    var reid: Int,
    var name: String,
    var type: Int
) : Parcelable