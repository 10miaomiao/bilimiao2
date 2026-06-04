package com.a10miaomiao.bilimiao.comm.entity.bangumi

import kotlinx.serialization.Serializable

@Serializable
data class DimensionXInfo(
    val height: Int,
    val rotate: Int,
    val width: Int
)