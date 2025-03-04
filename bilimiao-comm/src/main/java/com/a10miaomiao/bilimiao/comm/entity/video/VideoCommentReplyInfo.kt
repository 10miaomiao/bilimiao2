package com.a10miaomiao.bilimiao.comm.entity.video

import android.os.Parcelable
import com.a10miaomiao.bilimiao.comm.entity.user.MemberInfo
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
data class VideoCommentReplyInfo(
    val action: Int,
    val assist: Int,
    val attr: Int,
    val content: Content,
    val count: Long,
    val ctime: Long,
    val dialog: Long,
    val dialog_str: String,
    val fansgrade: Int,
    val like: Long,
    val member: MemberInfo,
    val mid: Long,
    val oid: Long, //--
    val parent: Long,
    val parent_str: String,
    val rcount: Long,
    val replies: List<VideoCommentReplyInfo>? = null,
    val root: Long,
    val root_str: String,
    val rpid: Long,
    val rpid_str: String, //--
    val reply_control: ReplyControl,
    val state: Int,
    val type: Int
): Parcelable {

    @Parcelize
    @Serializable
    data class Content(
//        val device: String,
        val message: String,
        val plat: Int = 0,
        val emote: Map<String, Emote>? = null,
    ) : Parcelable

    @Parcelize
    @Serializable
    data class Emote(
        val id: Long,
        val text: String,
        val url: String
    ) : Parcelable

    @Parcelize
    @Serializable
    data class ReplyControl(
        val time_desc: String? = null,
        val location: String? = null,
    ): Parcelable

}