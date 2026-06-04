package com.a10miaomiao.bilimiao.comm.entity.region

import kotlinx.serialization.Serializable

@Serializable
data class RegionVideosRankInfo(
    val result: List<RegionVideoInfo>?
)
