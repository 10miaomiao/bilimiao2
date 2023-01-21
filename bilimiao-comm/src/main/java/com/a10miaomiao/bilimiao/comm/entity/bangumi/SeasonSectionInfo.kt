package com.a10miaomiao.bilimiao.comm.entity.bangumi

data class SeasonSectionInfo (
    val main_section: SectionInfo,
    val section: List<SectionInfo>,
) {
    data class SectionInfo(
        val episodes: List<EpisodeInfo>,
        val id: String,
        val title: String,
        val type: Int,
    )

}