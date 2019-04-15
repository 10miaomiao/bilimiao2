package com.a10miaomiao.bilimiao.entity

data class Stat(
        val aid: String,
        val coin: Int,
        val danmaku: String,
        val dislike: Int,
        val favorite: Int,
        val his_rank: Int,
        val like: Int,
        val now_rank: Int,
        val reply: Int,
        val share: Int,
        val view: String
) {
    constructor(danmaku: String, view: String) : this(
            "", 0, danmaku,
            0, 0, 0, 0,
            0, 0, 0, view
    )
}