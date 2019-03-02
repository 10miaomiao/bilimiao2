package com.a10miaomiao.bilimiao.entity


data class ResultInfo<T>(
    val code: Int,
    val `data`: T,
    val message: String,
    val ttl: Int
)