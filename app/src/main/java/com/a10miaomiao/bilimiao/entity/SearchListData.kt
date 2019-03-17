package com.a10miaomiao.bilimiao.entity

data class SearchListData<T>(
        var pages: Int,
        var items: List<T>
)