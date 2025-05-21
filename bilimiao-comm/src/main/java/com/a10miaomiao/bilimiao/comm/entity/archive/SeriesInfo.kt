package com.a10miaomiao.bilimiao.comm.entity.archive

import kotlinx.serialization.Serializable

@Serializable
data class SeriesInfo(
    val type: String,
    val title: String,
    val cover: String,
    val param: String,
    val uri: String,
    val goto: String,
    val count: Int,
    val mtime: Long,
    val is_pay: Boolean,
)