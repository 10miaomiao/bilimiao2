package com.a10miaomiao.bilimiao.widget.rangedate.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SelectDateInfo(

    var startDate: Long, //入住日期
    var endDate: Long, //离店日期
    var count: Int, //天数
    var type: Int = RoomType.TYPE_ROOM_NORMAL.type,
    var hourDate: Long = 0 //钟点房

) : Parcelable