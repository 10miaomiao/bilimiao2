package com.a10miaomiao.bilimiao.comm.entity.bangumi

import kotlinx.serialization.Serializable

@Serializable
data class SeriesInfo (
    val count: Int,
    val id: Int,
    val title: String,
)