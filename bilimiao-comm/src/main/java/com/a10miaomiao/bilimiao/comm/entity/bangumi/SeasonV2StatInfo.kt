package com.a10miaomiao.bilimiao.comm.entity.bangumi

import kotlinx.serialization.Serializable

@Serializable
data class SeasonV2StatInfo(
    val coins: String,
    val danmakus: String,
    val favorite: String,
    val favorites: String,
    val followers: String,
    val likes: String,
    val play: String,
    val reply: String,
    val share: String,
    val views: String,
    val vt: String,
)