package com.a10miaomiao.bilimiao.comm.entity.search

data class SearchListInfo<T>(
    var pages: Int,
    var items: List<T>?,
)
