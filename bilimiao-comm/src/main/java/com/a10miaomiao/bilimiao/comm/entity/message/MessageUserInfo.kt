package com.a10miaomiao.bilimiao.comm.entity.message

data class MessageUserInfo(
    /**
     * 用户ID.
     */
    val mid: Long,
    /**
     * 是否为粉丝，0-不是，1-是.
     */
    val fans: Int,
    val nickname: String,
    val avatar: String,
    val follow: Boolean
)