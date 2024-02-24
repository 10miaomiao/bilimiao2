package com.a10miaomiao.bilimiao.comm.entity.media

data class MediaResponseV2Info(
    val media_list: List<MediaListV2Info>?,
    val has_more: Boolean,
    val total_count: Int,
    val next_start_key: String
)