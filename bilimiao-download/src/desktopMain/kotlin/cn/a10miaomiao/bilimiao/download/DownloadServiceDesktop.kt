package cn.a10miaomiao.bilimiao.download

import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

/**
 * 桌面端下载服务：基于 [BaseDownloadService]，使用 SupervisorJob+IO 协程作用域，
 * 桌面端不弹出系统通知（状态可通过 [curDownload] StateFlow 在 UI 中观察）。
 *
 * 下载目录：`PlatformProviders.context.filesDir/download`，与应用其他持久化数据共存。
 */
class DownloadServiceDesktop(
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : BaseDownloadService(scope) {

    override val notifier: DownloadNotifier = NoopDownloadNotifier

    override val downloadPathProvider: DownloadPathProvider = object : DownloadPathProvider {
        override val downloadPath: String = run {
            val dir = File(PlatformProviders.context.filesDir, "download")
            if (!dir.exists()) dir.mkdirs()
            dir.canonicalPath
        }
    }

    init {
        initialize()
    }

    companion object {
        @Volatile private var _instance: DownloadServiceDesktop? = null

        val instance: DownloadServiceDesktop
            get() = _instance ?: synchronized(this) {
                _instance ?: DownloadServiceDesktop().also { _instance = it }
            }
    }
}
