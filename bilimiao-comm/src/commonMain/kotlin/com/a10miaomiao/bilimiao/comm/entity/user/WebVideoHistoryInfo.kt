package com.a10miaomiao.bilimiao.comm.entity.user

data class WebVideoHistoryInfo(
    val cursor: CursorInfo,
    val list: List<ItemInfo>,
) {
    data class CursorInfo(
        val max: Long,
        val view_at: Long,
        val business: String,
        val ps: Int,
    )

    data class ItemInfo(
        val title: String,
        val cover: String,
        val author_name: String,
        val author_face: String,
        val view_at: Long,
        val history: ItemHistoryInfo,
    )

    data class ItemHistoryInfo(
        val oid: String,
        val bvid: String,
    )
}