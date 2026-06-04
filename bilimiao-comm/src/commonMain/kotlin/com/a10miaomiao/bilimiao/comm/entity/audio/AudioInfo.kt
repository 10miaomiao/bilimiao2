package com.a10miaomiao.bilimiao.comm.entity.audio

import kotlinx.serialization.Serializable

@Serializable
data class AudioInfo(
    var id: String,
    var uname: String,
    var author: String,
    var title: String,
    var cover_url: String
)
