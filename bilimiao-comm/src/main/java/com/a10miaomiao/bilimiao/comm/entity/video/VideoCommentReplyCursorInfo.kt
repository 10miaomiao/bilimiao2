package com.a10miaomiao.bilimiao.comm.entity.video

data class VideoCommentReplyCursorInfo(
    val assist: Int,
    val blacklist: Int,
    val config: VideoCommentInfo.Config,
    val cursor: CursorInfo,
    val root: VideoCommentReplyInfo,
    val upper: VideoCommentInfo.Upper
) {
    data class CursorInfo(
        val all_count: Int,
        val max_id: Int,
        val min_id: Int,
        val size: Int,
    )
}