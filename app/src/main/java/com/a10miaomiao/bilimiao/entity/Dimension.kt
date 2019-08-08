package com.a10miaomiao.bilimiao.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Dimension(
    val height: Int,
    val rotate: Int,
    val width: Int
): Parcelable