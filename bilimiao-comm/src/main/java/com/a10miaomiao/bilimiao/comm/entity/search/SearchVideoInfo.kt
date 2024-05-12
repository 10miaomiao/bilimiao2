package com.a10miaomiao.bilimiao.comm.entity.search

data class SearchVideoInfo(
    val author: String,
    val cover: String,
    val danmaku: String,
    val desc: String,
    val duration: String,
    val goto: String,
    val `param`: String,
    val play: String,
    val status: Int,
    val title: String?,
    val total_count: Long,
    val uri: String,
    val mid: String?,
)