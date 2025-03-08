package com.a10miaomiao.bilimiao.comm.entity.media

import kotlinx.serialization.Serializable

@Serializable
data class MediaUpperInfo(
    val mid: String,
    val name: String,
    val face: String,
)
