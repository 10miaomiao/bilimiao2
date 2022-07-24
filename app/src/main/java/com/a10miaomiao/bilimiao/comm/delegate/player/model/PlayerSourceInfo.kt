package com.a10miaomiao.bilimiao.comm.delegate.player.model

data class PlayerSourceInfo(
    val url: String,
    val quality: Int,
    val acceptList: List<AcceptInfo>
) {
    data class AcceptInfo(
        val quality: Int,
        val description: String,
    )
}