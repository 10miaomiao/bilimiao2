package com.a10miaomiao.bilimiao.comm.entity.message

data class UnreadMessageInfo(
    val at: Int,
    val chat: Int,
    val like: Int,
    val reply: Int,
    val sys_msg: Int
)