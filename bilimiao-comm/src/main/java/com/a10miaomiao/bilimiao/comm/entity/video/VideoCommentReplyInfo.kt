package com.a10miaomiao.bilimiao.comm.entity.video

import android.os.Parcel
import android.os.Parcelable
import com.a10miaomiao.bilimiao.comm.entity.user.MemberInfo
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import kotlinx.android.parcel.Parcelize


@Parcelize
data class VideoCommentReplyInfo(
    val action: Int,
    val assist: Int,
    val attr: Int,
    val content: Content,
    val count: Int,
    val ctime: Long,
    val dialog: Long,
    val dialog_str: String,
    val fansgrade: Int,
    val floor: Int,
    val like: Int,
    val member: MemberInfo,
    val mid: String,
    val oid: String, //--
    val parent: Long,
    val parent_str: String,
    val rcount: Long,
    val replies: List<VideoCommentReplyInfo>,
    val root: Long,
    val root_str: String,
    val rpid: Long,
    val rpid_str: String, //--
    val reply_control: ReplyControl,
    val state: Int,
    val type: Int
): Parcelable {

    @Parcelize
    data class Content(
//        val device: String,
        val message: String,
        val plat: Int,
        val emote: Map<String, Emote>?,
    ) : Parcelable

    @Parcelize
    data class Emote(
        val id: Int,
        val text: String,
        val url: String
    ) : Parcelable

    @Parcelize
    data class ReplyControl(
        val time_desc: String,
        val location: String?,
    ): Parcelable

}