package com.a10miaomiao.bilimiao.comm.entity.video

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UgcSeasonInfo(
    val id: String,
    val title: String,
    val cover: String,
    val intro: String,
    val sections: List<UgcSectionInfo>,
): Parcelable