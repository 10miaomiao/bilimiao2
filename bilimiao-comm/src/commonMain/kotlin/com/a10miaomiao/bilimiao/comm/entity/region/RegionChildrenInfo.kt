package com.a10miaomiao.bilimiao.comm.entity.region

import kotlinx.serialization.Serializable

/**
 * 子分区信息
 */
@Serializable
data class RegionChildrenInfo(
    var tid: Int,
    var reid: Int,
    var name: String,
    var type: Int = 0
)