package com.a10miaomiao.bilimiao.comm.entity.message

data class LikeMesageLatestInfo(
    val items: List<LikeMessageInfo>,
    val last_view_at: Int,
)