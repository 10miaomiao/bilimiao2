package cn.a10miaomiao.bilimiao.compose.common.download.entry

import kotlinx.serialization.Serializable

@Serializable
data class BiliDownloadEntryAndPathInfo(
    val pageDirPath: String,
    val entryDirPath: String,
    val entry: BiliDownloadEntryInfo,
)
