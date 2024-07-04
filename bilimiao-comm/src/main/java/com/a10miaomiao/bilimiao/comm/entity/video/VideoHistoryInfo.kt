package com.a10miaomiao.bilimiao.comm.entity.video

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VideoHistoryInfo(
    val cid: String,
    val progress: Int,
): Parcelable
