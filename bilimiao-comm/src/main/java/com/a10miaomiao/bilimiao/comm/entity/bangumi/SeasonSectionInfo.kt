package com.a10miaomiao.bilimiao.comm.entity.bangumi

import kotlinx.serialization.Serializable

@Serializable
data class SeasonSectionInfo (
    val main_section: SectionInfo?,
    val section: List<SectionInfo>,
) {

    @Serializable
    data class SectionInfo(
        val episodes: List<EpisodeInfo>,
        val id: String,
        val title: String,
        val type: Int,
    )

}