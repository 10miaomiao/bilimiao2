package com.a10miaomiao.bilimiao.entity

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Page(
    val cid: String,
    val dimension: Dimension,
//    val dm: Dm,
    val dmlink: String,
    val duration: Int,
    val from: String,
    val metas: List<Meta>,
    val page: Int,
    var part: String,
    val vid: String,
    val weblink: String
): Parcelable