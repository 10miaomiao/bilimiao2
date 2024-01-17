package com.a10miaomiao.bilimiao.comm.entity.message

data class LikeMessageTotalInfo(
    val cursor: MessageCursorInfo,
    val items: List<LikeMessageInfo>
)
