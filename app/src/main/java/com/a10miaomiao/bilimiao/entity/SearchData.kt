package com.a10miaomiao.bilimiao.entity

data class SearchData<T>(
        var trackid: String,
        var page: Int,
        var items: T,
        var attribute: Int
)