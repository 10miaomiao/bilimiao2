package com.a10miaomiao.bilimiao.comm.entity.message

import kotlinx.serialization.Serializable

@Serializable
data class UnreadMessageInfo(
    val at: Int,
    val chat: Int,
    val like: Int,
    val reply: Int,
    val sys_msg: Int
)