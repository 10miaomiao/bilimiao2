package com.a10miaomiao.bilimiao.comm.entity.video

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class VideoStaffInfo (
    var mid: String,
    var title: String,
    var face: String,
    var name: String
) : Parcelable