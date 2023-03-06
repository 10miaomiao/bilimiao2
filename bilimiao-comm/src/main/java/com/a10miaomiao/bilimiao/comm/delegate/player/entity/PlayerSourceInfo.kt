package com.a10miaomiao.bilimiao.comm.delegate.player.entity

data class PlayerSourceInfo(
    val url: String,
    val quality: Int,
    val acceptList: List<AcceptInfo>,
    val duration: Long,
) {

    val description: String get() = acceptList.find { it.quality == quality }?.description ?: "未知清晰度"

    data class AcceptInfo(
        val quality: Int,
        val description: String,
    )
}