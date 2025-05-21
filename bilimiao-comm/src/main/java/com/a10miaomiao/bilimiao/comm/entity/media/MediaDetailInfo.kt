package com.a10miaomiao.bilimiao.comm.entity.media

import kotlinx.serialization.Serializable

@Serializable
data class MediaDetailInfo(
    val info: MediaListInfo,
    val medias: List<MediasInfo>? = null,
)