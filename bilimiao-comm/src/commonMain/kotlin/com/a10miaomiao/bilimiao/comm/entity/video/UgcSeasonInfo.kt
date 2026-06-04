package com.a10miaomiao.bilimiao.comm.entity.video

import kotlinx.serialization.Serializable

@Serializable
data class UgcSeasonInfo(
    val id: String,
    val title: String,
    val cover: String,
    val intro: String,
    val sections: List<UgcSectionInfo>,
)