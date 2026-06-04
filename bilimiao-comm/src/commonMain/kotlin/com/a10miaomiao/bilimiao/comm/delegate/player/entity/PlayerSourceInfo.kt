package com.a10miaomiao.bilimiao.comm.delegate.player.entity

class PlayerSourceInfo {
    var url: String = ""
    var quality: Int = 0
    var acceptList: List<AcceptInfo> = emptyList()
    var duration: Long = 0L
    var header: Map<String, String> = emptyMap()

    val description: String get() = acceptList.find { it.quality == quality }?.description ?: "未知清晰度"

    var height = 900 // 默认 16:9
    var width = 1600
    val screenProportion get() = width.toFloat() / height.toFloat() // 视频画面比例
    var lastPlayTime = 0L
    var lastPlayCid = ""

    data class AcceptInfo(
        val quality: Int,
        val description: String,
    )
}