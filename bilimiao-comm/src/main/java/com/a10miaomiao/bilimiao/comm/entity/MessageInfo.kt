package com.a10miaomiao.bilimiao.comm.entity

import kotlinx.serialization.Serializable

@Serializable
data class MessageInfo(
    val code: Int,
    val message: String,
    val ttl: Int = 0,
) {
    // 响应结果是否为成功
    val isSuccess get() = code == 0
}
