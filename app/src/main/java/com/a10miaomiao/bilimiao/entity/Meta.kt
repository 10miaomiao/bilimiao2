package com.a10miaomiao.bilimiao.entity

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Meta(
    val format: String,
    val quality: Int,
    val size: Int
): Parcelable