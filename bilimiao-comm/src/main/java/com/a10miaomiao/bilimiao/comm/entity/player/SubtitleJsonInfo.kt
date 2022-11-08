package com.a10miaomiao.bilimiao.comm.entity.player

data class SubtitleJsonInfo(
    val font_size: Float,
    val font_color: String,
    val background_alpha: Float,
    val background_color: String,
    val Stroke: String,
    val body: List<ItemInfo>,
) {
    data class ItemInfo(
        val from: Double,
        val to: Double,
        val location: Int,
        val content: String,
    )
}