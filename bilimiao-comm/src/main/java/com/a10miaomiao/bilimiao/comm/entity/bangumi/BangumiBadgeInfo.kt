package com.a10miaomiao.bilimiao.comm.entity.bangumi

import kotlinx.serialization.Serializable

@Serializable
data class BangumiBadgeInfo(
    val bg_color: String,
    val bg_color_night: String,
    val text: String,
)
