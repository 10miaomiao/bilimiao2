package com.a10miaomiao.bilimiao.comm.entity.live

import kotlinx.serialization.Serializable

@Serializable
data class RoomInfo(
    val uid: String,
    val room_id: String,
    val short_id: String,
    val background: String,
    val title: String,
    val user_cover: String,
    val keyframe: String,
)
