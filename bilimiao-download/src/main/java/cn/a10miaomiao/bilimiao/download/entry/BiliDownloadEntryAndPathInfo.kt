package cn.a10miaomiao.bilimiao.download.entry

data class BiliDownloadEntryAndPathInfo(
    val pageDirPath: String,
    val entryDirPath: String,
    val entry: BiliDownloadEntryInfo,
)
