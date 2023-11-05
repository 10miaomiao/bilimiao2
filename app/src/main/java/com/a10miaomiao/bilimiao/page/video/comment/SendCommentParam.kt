package com.a10miaomiao.bilimiao.page.video.comment

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SendCommentParam(
    val type: Int,
    val oid: String,
    val root: String? = null,
    val parent: String? = null,
    val title: String,
    val image: String,
    val content: String,
    val name: String,
): Parcelable