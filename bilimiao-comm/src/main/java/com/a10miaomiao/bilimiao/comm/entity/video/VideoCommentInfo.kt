package com.a10miaomiao.bilimiao.comm.entity.video

data class VideoCommentInfo(
    val assist: Int,
    val blacklist: Int,
    val config: Config,
    val page: Page,
//    val hots: List<VideoCommentReplyInfo>,
    val notice: Any,
    val replies: List<VideoCommentReplyInfo>,
    val root: VideoCommentReplyInfo?,
    val top: Top,
    val upper: Upper
)  {

    data class Config(
        val showadmin: Int,
        val showentry: Int
    )

    data class Page(
        var num: Int,
        var size: Int,
        var count: Int,
        var acount: Int
    )

    data class Top(
        val admin: Any,
        val upper: Any
    )

    data class Upper(
        val mid: Int
    )
}