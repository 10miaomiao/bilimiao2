package com.a10miaomiao.bilimiao.page.video.comment

import android.os.Parcelable
import com.a10miaomiao.bilimiao.commponents.comment.VideoCommentViewContent
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VideoCommentDetailParam(
    val index: Int,
    val oid: Long,
    val rpid: Long,
    val mid: Long,
    val uname: String,
    val avatar: String,
    val ctime: Long,
    val floor: Int,
    val location: String,
    val content: VideoCommentViewContent,
    val like: Long,
    val count: Long,
    val action: Long,
): Parcelable
