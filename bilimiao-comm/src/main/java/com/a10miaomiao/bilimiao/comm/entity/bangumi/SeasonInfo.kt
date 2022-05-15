package com.a10miaomiao.bilimiao.comm.entity.bangumi

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SeasonInfo(
    val is_new: Int,
    val season_id: String,
    val season_title: String,
    val title: String,

    val is_jump: Int,
) : Parcelable