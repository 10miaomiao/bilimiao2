package com.a10miaomiao.bilimiao.comm.entity

data class ItemAndCountInfo<T> (
    val count: Int,
    val item: List<T>,
)
