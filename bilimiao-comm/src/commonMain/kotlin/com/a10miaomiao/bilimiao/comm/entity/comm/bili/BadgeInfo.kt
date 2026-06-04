package com.a10miaomiao.bilimiao.comm.entity.comm.bili

import kotlinx.serialization.Serializable

@Serializable
data class BadgeInfo(
    val bg_color: String,
    val bg_color_night: String,
    val text: String,
)