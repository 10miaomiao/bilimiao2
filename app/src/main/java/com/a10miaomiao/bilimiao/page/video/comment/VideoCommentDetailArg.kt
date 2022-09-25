package com.a10miaomiao.bilimiao.page.video.comment

import android.os.Parcelable
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentReplyInfo
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VideoCommentDetailArg(
    val oid: String,
    val rpid: Long,
    val rpid_str: String,
    val mid: String,
    val uname: String,
    val avatar: String,
    val ctime: Long,
    val floor: Int,
    val content: VideoCommentReplyInfo.Content,
    val like: Int,
    val count: Int,
): Parcelable
