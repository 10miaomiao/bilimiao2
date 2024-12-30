package com.a10miaomiao.bilimiao.comm.entity.auth

import kotlinx.serialization.Serializable

@Serializable
data class CaptchaPreInfo(
    val recaptcha_token: String,
    val gee_gt: String,
    val gee_challenge: String,
)