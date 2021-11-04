package com.a10miaomiao.bilimiao.comm.entity.video

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VideoOwnerInfo(
    val face: String,
    val mid: Long,
    val name: String
) : Parcelable