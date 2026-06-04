package com.a10miaomiao.bilimiao.comm.entity.message

import kotlinx.serialization.Serializable

@Serializable
data class LikeMesageLatestInfo(
    val items: List<LikeMessageInfo>,
    val last_view_at: Int,
)