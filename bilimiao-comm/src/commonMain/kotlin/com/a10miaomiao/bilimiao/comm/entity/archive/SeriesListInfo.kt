package com.a10miaomiao.bilimiao.comm.entity.archive

import kotlinx.serialization.Serializable

@Serializable
data class SeriesListInfo(
    val items: List<SeriesInfo>,
    val page: PageInfo,
) {

    @Serializable
    data class PageInfo(
        val page_num: Int,
        val page_size: Int,
        val total: Int,
    )
}