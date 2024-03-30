package com.a10miaomiao.bilimiao.comm.entity.video

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UgcSectionInfo(
    val id: String,
    val title: String,
    val type: Int,
    val episodes: List<UgcEpisodeInfo>,
): Parcelable