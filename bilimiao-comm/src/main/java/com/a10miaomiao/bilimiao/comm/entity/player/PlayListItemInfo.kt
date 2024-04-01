package com.a10miaomiao.bilimiao.comm.entity.player

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlayListItemInfo(
    val aid: String,
    val cid: String,
    val duration: Int,
    val title: String,
    val cover: String,
    val ownerId: String,
    val ownerName: String,
    val from: String,
) : Parcelable