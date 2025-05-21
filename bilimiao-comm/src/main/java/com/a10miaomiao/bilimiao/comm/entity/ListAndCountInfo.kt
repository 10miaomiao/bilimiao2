package com.a10miaomiao.bilimiao.comm.entity

import kotlinx.serialization.Serializable

@Serializable
data class ListAndCountInfo<T> (
    val count: Int,
    val list: List<T>,
    val has_more: Boolean,
)