package com.a10miaomiao.bilimiao.entity

data class ResultInfo2<T>(
        val code: Int,
        val result: T,
        val message: String,
        val ttl: Int
)