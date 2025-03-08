package com.a10miaomiao.bilimiao.comm.entity.media

import kotlinx.serialization.Serializable

@Serializable
data class MediaResponseInfo (
    var count: Int,
    var list: List<MediaListInfo>,
)