package com.a10miaomiao.bilimiao.page.download

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DownloadVideoCreateParam(
    val aid: String,
    val bvid: String,
    val title: String,
    val pic: String,
    val mid: String,
    val pages: List<Page>,
): Parcelable {

    @Parcelize
    data class Page(
        val cid: String,
        val page: Int,
        val from: String,
        val part: String,
        var vid: String,
    ): Parcelable
}
