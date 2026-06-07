package cn.a10miaomiao.bilimiao.compose.common.download

import cn.a10miaomiao.bilimiao.compose.common.download.entry.BiliDownloadEntryAndPathInfo
import cn.a10miaomiao.bilimiao.compose.common.download.entry.BiliDownloadEntryInfo
import cn.a10miaomiao.bilimiao.compose.common.download.entry.CurrentDownloadInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DownloadManagerDesktop : DownloadManager {

    override val downloadListVersion: StateFlow<Int> = MutableStateFlow(0)
    override val curDownload: StateFlow<CurrentDownloadInfo?> = MutableStateFlow(null)
    override val downloadList: List<BiliDownloadEntryAndPathInfo> = emptyList()

    override fun getDownloadPath(): String = ""
    override fun readDownloadDirectory(dirPath: String): List<BiliDownloadEntryAndPathInfo> = emptyList()
    override fun createDownload(biliEntry: BiliDownloadEntryInfo) {}
    override fun startDownload(entryDirPath: String) {}
    override fun cancelDownload(taskId: Long) {}
    override fun deleteDownload(pageDirPath: String, entryDirPath: String) {}
}
