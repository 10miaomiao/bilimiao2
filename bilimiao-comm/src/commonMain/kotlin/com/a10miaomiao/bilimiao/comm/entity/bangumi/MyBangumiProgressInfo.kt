package com.a10miaomiao.bilimiao.comm.entity.bangumi

import kotlinx.serialization.Serializable

@Serializable
data class MyBangumiProgressInfo (
    val index_show: String,
    val last_ep_id: Int,
    val last_time: Int,
)