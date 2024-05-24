package com.a10miaomiao.bilimiao.comm.entity.player

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlayListInfo(
    val name: String,
    val from: String,
    val items: List<PlayListItemInfo>,
    val type: Int, // 1:合集 2:收藏
) : Parcelable