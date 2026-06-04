package com.a10miaomiao.bilimiao.comm.entity.history

import kotlinx.serialization.Serializable

@Serializable
data class ToViewInfo(
    val show_count: Int,
    val list: List<ToViewItemInfo>,
    val has_more: Boolean,
    val next_key: String = "",
    val split_key: String = "",
)