package com.a10miaomiao.bilimiao.comm.entity.comm.bili

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BadgeInfo(
    val bg_color: String,
    val bg_color_night: String,
    val text: String,
) : Parcelable