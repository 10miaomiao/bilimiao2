package com.a10miaomiao.bilimiao.entity.comment

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LevelInfo(
        val current_exp: Int,
        val current_level: Int,
        val current_min: Int,
        val next_exp: Int
): Parcelable