package com.a10miaomiao.bilimiao.comm.entity.message

data class MessageResponseInfo<T>(
    val cursor: MessageCursorInfo,
    val items: List<T>,
    val last_view_at: Long
)