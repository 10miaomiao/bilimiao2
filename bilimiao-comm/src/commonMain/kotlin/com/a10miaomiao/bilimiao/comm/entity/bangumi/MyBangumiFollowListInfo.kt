package com.a10miaomiao.bilimiao.comm.entity.bangumi

import kotlinx.serialization.Serializable

@Serializable
data class MyBangumiFollowListInfo(
    val follow_list: List<MyBangumiInfo>? = null,
    val has_next: Int,
    val total: Int,
)
