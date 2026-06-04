package com.a10miaomiao.bilimiao.comm.entity.user

import com.a10miaomiao.bilimiao.comm.entity.media.MediaInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListInfo

data class UserSpaceFavFolderInfo (
    val space_infos: List<MediaInfo>,
    val default_folder: DefaultFolderInfo,
) {
    data class DefaultFolderInfo(
        val folder_detail: MediaListInfo,
    )
}