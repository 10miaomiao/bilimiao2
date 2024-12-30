package com.a10miaomiao.bilimiao.comm.entity.auth

import kotlinx.serialization.Serializable

@Serializable
data class SmsSendInfo(
    val recaptcha_url: String?,
    val captcha_key: String
)