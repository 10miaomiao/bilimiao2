package com.a10miaomiao.bilimiao.comm.entity.search

data class SearchResultInfo<T>(
    var trackid: String,
    var page: Int,
    var items: T,
    var attribute: Int
)