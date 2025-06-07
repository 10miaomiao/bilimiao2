package com.a10miaomiao.bilimiao.comm.entity.player

import kotlinx.serialization.Serializable

@Serializable
data class SubtitleJsonInfo(
//    val font_size: Float = 1f,
//    val font_color: String = "#fff",
//    val background_alpha: Float = 1f,
//    val background_color: String = "#000",
    val Stroke: String = "",
    val body: List<ItemInfo>,
) {
    @Serializable
    data class ItemInfo(
        val from: Double,
        val to: Double,
        val location: Int,
        val content: String,
    )
}