package com.a10miaomiao.bilimiao.comm.entity

import kotlinx.serialization.Serializable

@Serializable
data class ResponseResult<T>(
    val code: Int,
    val result: T? = null,
    val message: String,
    val ttl: Int = 0,
) {
    // 响应结果是否为成功
    val isSuccess get() = code == 0

    fun requireData() = result!!
}
