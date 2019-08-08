package com.a10miaomiao.bilimiao.entity

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Vip(
    val accessStatus: Int,
    val dueRemark: String,
    val vipDueDate: Double,
    val vipStatus: Int,
    val vipStatusWarn: String,
    val vipType: Int
): Parcelable