package cn.a10miaomiao.bilimiao.compose.common.download

import cn.a10miaomiao.bilimiao.compose.common.download.entry.BiliDownloadEntryAndPathInfo
import cn.a10miaomiao.bilimiao.compose.common.download.entry.BiliDownloadEntryInfo
import cn.a10miaomiao.bilimiao.compose.common.download.entry.CurrentDownloadInfo
import kotlinx.coroutines.flow.StateFlow

interface DownloadManager {
    val downloadList: List<BiliDownloadEntryAndPathInfo>
    val downloadListVersion: StateFlow<Int>
    val curDownload: StateFlow<CurrentDownloadInfo?>

    fun getDownloadPath(): String
    fun readDownloadDirectory(dirPath: String): List<BiliDownloadEntryAndPathInfo>
    fun createDownload(biliEntry: BiliDownloadEntryInfo)
    fun startDownload(entryDirPath: String)
    fun cancelDownload(taskId: Long)
    fun deleteDownload(pageDirPath: String, entryDirPath: String)
}
