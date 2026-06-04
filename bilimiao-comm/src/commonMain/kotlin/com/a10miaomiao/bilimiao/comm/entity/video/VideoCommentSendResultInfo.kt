package com.a10miaomiao.bilimiao.comm.entity.video

import kotlinx.serialization.Serializable

@Serializable
class VideoCommentSendResultInfo(
    val need_captcha: Boolean,
    val url: String,
    val success_action: Int,
    val success_toast: String,
    val success_animation: String,
    val rpid: Long,
    val rpid_str: String,
    val dialog: Long,
    val dialog_str: String,
    val root: Long,
    val root_str: String,
    val parent: Long,
    val parent_str: String,
    val reply: VideoCommentReplyInfo,
)