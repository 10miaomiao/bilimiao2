package com.a10miaomiao.bilimiao.comm.entity.auth

import kotlinx.serialization.Serializable

@Serializable
data class QRLoginInfo (
    val url: String,
    val auth_code: String,
)