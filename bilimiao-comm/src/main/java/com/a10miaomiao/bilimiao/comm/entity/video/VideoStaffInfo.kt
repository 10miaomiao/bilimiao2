package com.a10miaomiao.bilimiao.comm.entity.video

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VideoStaffInfo (
    var mid: String,
    var title: String,
    var face: String,
    var name: String
) : Parcelable