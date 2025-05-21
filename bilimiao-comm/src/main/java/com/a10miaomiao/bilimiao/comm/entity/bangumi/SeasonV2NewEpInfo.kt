package com.a10miaomiao.bilimiao.comm.entity.bangumi

import kotlinx.serialization.Serializable

@Serializable
data class SeasonV2NewEpInfo (
    val desc: String,
    val id: String,
    val is_new: Int,
    val more: String,
    val title: String,
)