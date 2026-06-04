package com.a10miaomiao.bilimiao.comm.entity.video

import kotlinx.serialization.Serializable

@Serializable
data class UgcSectionInfo(
    val id: String,
    val title: String,
    val type: Int,
    val episodes: List<UgcEpisodeInfo>,
)
