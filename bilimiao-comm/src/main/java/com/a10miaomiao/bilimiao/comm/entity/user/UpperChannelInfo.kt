package com.a10miaomiao.bilimiao.comm.entity.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class UpperChannelInfo(
    var cid : String,
    var mid : String,
    var name : String,
    var intro : String,
    var mtime : Long,
    var count : Int,
    var cover : String,
//        var archives : List<UpperArchives>,
    var isAll: Boolean = false
): Parcelable