package com.a10miaomiao.bilimiao.comm.entity.bangumi

import kotlinx.serialization.Serializable

@Serializable
data class SeasonV2Progress(
    val last_ep_id: String,
    val last_ep_index: String,
    val last_time: Int,
)
