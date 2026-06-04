package com.a10miaomiao.bilimiao.comm.entity.bangumi

import kotlinx.serialization.Serializable

@Serializable
data class MyBangumiNewEpInfo(
    val cover: String,
    val duration: Int,
    val id: Int,
    val index_show: String,
    val is_new: Int
)