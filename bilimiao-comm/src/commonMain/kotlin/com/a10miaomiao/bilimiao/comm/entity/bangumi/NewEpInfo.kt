package com.a10miaomiao.bilimiao.comm.entity.bangumi

import kotlinx.serialization.Serializable

@Serializable
data class NewEpInfo(
    val desc: String,
    val id: Int,
    val is_new: Int,
    val title: String
)
