package com.a10miaomiao.bilimiao.comm.entity.message

import kotlinx.serialization.Serializable

@Serializable
data class LikeMessageResponseInfo (
    val latest: LikeMesageLatestInfo,
    val total: LikeMessageTotalInfo,
)