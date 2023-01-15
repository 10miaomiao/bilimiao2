package com.a10miaomiao.bilimiao.comm.entity.auth

data class CaptchaPreInfo(
    val recaptcha_token: String,
    val gee_gt: String,
    val gee_challenge: String,
)