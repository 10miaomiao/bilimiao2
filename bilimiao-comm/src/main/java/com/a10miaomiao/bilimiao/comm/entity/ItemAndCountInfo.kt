package com.a10miaomiao.bilimiao.comm.entity

import kotlinx.serialization.Serializable

@Serializable
data class ItemAndCountInfo<T> (
    val count: Int,
    val item: List<T>,
)
