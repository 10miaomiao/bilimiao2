package com.a10miaomiao.bilimiao.comm.entity

data class ListAndCountInfo<T> (
    val count: Int,
    val list: List<T>,
    val has_more: Boolean,
)