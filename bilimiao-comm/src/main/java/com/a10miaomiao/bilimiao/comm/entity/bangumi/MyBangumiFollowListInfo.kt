package com.a10miaomiao.bilimiao.comm.entity.bangumi

data class MyBangumiFollowListInfo(
    val follow_list: List<MyBangumiInfo>,
    val has_next: Int,
    val total: Int,
)
