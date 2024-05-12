package com.a10miaomiao.bilimiao.comm.entity.media

data class MediaFoldersInfo(
    val folders: List<MediaFolderDetailInfo>,
    val medias: List<MediasInfo>?,
    val has_more: Boolean,
)
