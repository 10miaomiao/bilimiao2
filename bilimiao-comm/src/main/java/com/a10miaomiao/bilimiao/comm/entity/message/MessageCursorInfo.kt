package com.a10miaomiao.bilimiao.comm.entity.message

import kotlinx.serialization.Serializable

@Serializable
data class MessageCursorInfo(
    /**
     * 是否已到末尾.
     */
    val isEnd: Boolean = false,

    /**
     * 标识符.
     */
    val id: Long,

    /**
     * 发生时间.
     */
    val time: Long
)
