package com.a10miaomiao.bilimiao.comm.entity.home

import kotlinx.serialization.Serializable

@Serializable
data class HomeRecommendInfo (
    val items: List<RecommendCardInfo>,
)