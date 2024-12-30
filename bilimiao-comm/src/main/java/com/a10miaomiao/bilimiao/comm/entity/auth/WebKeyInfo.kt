package com.a10miaomiao.bilimiao.comm.entity.auth

import kotlinx.serialization.Serializable

@Serializable
data class WebKeyInfo(
    val hash: String,
    val key: String,
)
