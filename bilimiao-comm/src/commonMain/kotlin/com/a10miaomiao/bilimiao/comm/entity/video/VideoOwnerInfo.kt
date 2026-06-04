package com.a10miaomiao.bilimiao.comm.entity.video

import kotlinx.serialization.Serializable

@Serializable
data class VideoOwnerInfo(
    val face: String,
    val mid: String,
    val name: String
)
