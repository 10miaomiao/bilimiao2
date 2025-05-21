package com.a10miaomiao.bilimiao.comm.entity.message

import kotlinx.serialization.Serializable

@Serializable
data class LikeMessageTotalInfo(
    val cursor: MessageCursorInfo,
    val items: List<LikeMessageInfo>
)
