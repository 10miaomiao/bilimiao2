package com.a10miaomiao.bilimiao.comm.entity.bangumi

data class SeasonV2ModuleInfo (
    val data: DataInfo,
    val id: Int,
    val style: String,
    val title: String,
) {
    data class DataInfo(
//        val episodes: List<EpisodeInfo>?,
        val seasons: List<SeasonInfo>?,
    )

}