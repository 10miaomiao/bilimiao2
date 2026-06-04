package com.a10miaomiao.bilimiao.comm.entity.bangumi

import kotlinx.serialization.Serializable

@Serializable
data class SeasonV2ModuleInfo (
    val data: DataInfo,
    val id: Int,
    val style: String,
    val title: String,
) {
    @Serializable
    data class DataInfo(
//        val episodes: List<EpisodeInfo>?,
        val seasons: List<SeasonInfo>? = null,
    )

}