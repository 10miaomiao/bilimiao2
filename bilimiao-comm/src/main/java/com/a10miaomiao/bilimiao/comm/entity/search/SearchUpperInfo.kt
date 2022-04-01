package com.a10miaomiao.bilimiao.comm.entity.search

data class SearchUpperInfo(
    var title: String,
    var cover: String,
    var uri: String,
    var param: String,
    var goto: String,
    var total_count: Int,
    var sign: String,
    var fans: Int,
    var archives: Int,
    var status: Int
)