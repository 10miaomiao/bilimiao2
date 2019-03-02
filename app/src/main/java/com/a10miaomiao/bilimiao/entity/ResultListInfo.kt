package com.a10miaomiao.bilimiao.entity

data class ResultListInfo<T> (
        var code: Int,
        var msg: String,
        var data: List<T>
)