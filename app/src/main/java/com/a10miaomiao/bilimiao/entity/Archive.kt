package com.a10miaomiao.bilimiao.entity

data class Archive(
        val author: String,
        val cover: String,
        val danmaku: Int,
        val desc: String,
        val duration: String,
        val goto: String,
        val `param`: String,
        val play: Int,
        val status: Int,
        val title: String,
        val total_count: Int,
        val uri: String,
        val mid: Long
)