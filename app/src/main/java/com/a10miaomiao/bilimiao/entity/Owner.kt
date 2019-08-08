package com.a10miaomiao.bilimiao.entity

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Owner(
    val face: String,
    val mid: Long,
    val name: String
) : Parcelable