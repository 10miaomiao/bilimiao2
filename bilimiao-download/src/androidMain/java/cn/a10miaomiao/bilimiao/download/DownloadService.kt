package cn.a10miaomiao.bilimiao.download

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import java.io.File
import kotlin.coroutines.CoroutineContext

class DownloadService : Service(), CoroutineScope {
    companion object {
        private const val TAG = "DownloadService"
        private val channel = Channel<DownloadService>()
        private var _instance: DownloadService? = null

        val instance get() = _instance

        suspend fun getService(context: Context): DownloadService {
            _instance?.let { return it }
            startService(context)
            return channel.receive().also {
                _instance = it
            }
        }

        fun startService(context: Context) {
            val intent = Intent(context, DownloadService::class.java)
            context.startService(intent)
        }
    }

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val _core by lazy {
        object : BaseDownloadService(this) {
            override val notifier = DownloadNotify(this@DownloadService)
            override val downloadPathProvider = object : DownloadPathProvider {
                override val downloadPath: String
                    get() = this@DownloadService.externalDownloadPath
            }
        }
    }

    val downloadList get() = _core.downloadList
    val downloadListVersion get() = _core.downloadListVersion
    val curDownload get() = _core.curDownload

    fun readDownloadList() = _core.readDownloadList()
    fun readDownloadDirectory(dir: File) = _core.readDownloadDirectory(dir)
    fun createDownload(entry: cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo) =
        _core.createDownload(entry)
    fun startDownload(entryDirPath: String) = _core.startDownload(entryDirPath)
    fun startDownload(info: cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryAndPathInfo) =
        _core.startDownload(info)
    fun cancelDownload(taskId: Long) = _core.cancelDownload(taskId)
    fun deleteDownload(pageDirPath: String, entryDirPath: String) =
        _core.deleteDownload(pageDirPath, entryDirPath)
    fun getDownloadPath(): String = _core.getDownloadPath()

    override fun onCreate() {
        super.onCreate()
        job = Job()
        _instance = this
        _core.initialize()
        // 在服务协程作用域中通知等待 getService 的调用者
        launch {
            channel.send(this@DownloadService)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        _instance = null
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    /**
     * 下载目录：与历史版本兼容，使用 externalFilesDir/../download。
     */
    private val externalDownloadPath: String
        get() {
            var file = File(getExternalFilesDir(null), "../download")
            if (!file.exists()) {
                file.mkdir()
            }
            return file.canonicalPath
        }
}
