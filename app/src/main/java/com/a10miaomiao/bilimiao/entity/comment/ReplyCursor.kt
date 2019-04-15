package com.a10miaomiao.bilimiao.entity.comment

data class ReplyCursor(
    val assist: Int,
    val blacklist: Int,
    val config: Config,
    val cursor: Cursor,
    val root: ReplyRoot,
    val upper: Upper
)

