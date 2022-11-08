package com.a10miaomiao.bilimiao.page.video

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VideoPagesParam(
    val aid: String,
    val pic: String,
    val title: String,
    val ownerId: String,
    val ownerName: String,
    val pages: List<Page>
): Parcelable {

    @Parcelize
    data class Page(
        val cid: String,
        val part: String,
    ): Parcelable

}
