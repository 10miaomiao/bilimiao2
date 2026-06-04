package com.a10miaomiao.bilimiao.comm.entity.home

import kotlinx.serialization.Serializable

@Serializable
data class RecommendCardPlayerArgsInfo (
    val aid: String,
    val cid: String,
    val duration: Long,
    val type: String,
)