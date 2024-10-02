package com.a10miaomiao.bilimiao.comm.entity.archive

data class SeriesListInfo(
    val items: List<SeriesInfo>,
    val page: PageInfo,
) {
    data class PageInfo(
        val page_num: Int,
        val page_size: Int,
        val total: Int,
    )
}