package com.a10miaomiao.bilimiao.comm.entity.message

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponseInfo<T>(
    val cursor: MessageCursorInfo,
    val items: List<T>,
    val last_view_at: Long = 0L,
)