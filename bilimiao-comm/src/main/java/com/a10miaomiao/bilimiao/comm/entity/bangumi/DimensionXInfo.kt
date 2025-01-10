package com.a10miaomiao.bilimiao.comm.entity.bangumi

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DimensionXInfo(
    val height: Int,
    val rotate: Int,
    val width: Int
) : Parcelable