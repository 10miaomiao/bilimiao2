package com.a10miaomiao.bilimiao.comm.entity.media

import kotlinx.serialization.Serializable

@Serializable
data class MediaFoldersInfo(
    val folders: List<MediaFolderDetailInfo>,
    val medias: List<MediasInfo>? = null,
    val has_more: Boolean,
)
