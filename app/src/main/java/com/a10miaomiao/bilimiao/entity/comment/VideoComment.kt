package com.a10miaomiao.bilimiao.entity.comment

data class VideoComment(
        val assist: Int,
        val blacklist: Int,
        val config: Config,
        val page: Page,
        val hots: List<ReplyBean>,
        val notice: Any,
        val replies: List<ReplyBean>,
        val top: Top,
        val upper: Upper
)