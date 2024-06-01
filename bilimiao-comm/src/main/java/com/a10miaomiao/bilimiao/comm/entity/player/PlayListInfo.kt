package com.a10miaomiao.bilimiao.comm.entity.player

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlayListInfo(
    val name: String,
    val from: PlayListFrom,
    val items: List<PlayListItemInfo>,
) : Parcelable