package com.a10miaomiao.bilimiao.entity.comment

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Content(
        val device: String,
        val message: String,
        val plat: Int,
        val emote: Map<String, Emote>
) : Parcelable

@Parcelize
data class Emote(
        val id: Int,
        val text: String,
        val url: String
) : Parcelable