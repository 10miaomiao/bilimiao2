package com.a10miaomiao.bilimiao.entity

data class ListCount<T>(
    var count: Int,
    var list: List<T>
)