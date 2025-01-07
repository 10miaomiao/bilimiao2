package com.a10miaomiao.bilimiao.comm.entity.history

import kotlinx.serialization.Serializable

@Serializable
data class ToViewInfo(
    val count: Int,
    val list: List<ToViewItemInfo>
)