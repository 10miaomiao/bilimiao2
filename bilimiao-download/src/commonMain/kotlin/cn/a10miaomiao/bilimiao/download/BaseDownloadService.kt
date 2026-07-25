package cn.a10miaomiao.bilimiao.download

import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryAndPathInfo
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadMediaFileInfo
import cn.a10miaomiao.bilimiao.download.entry.CurrentDownloadInfo
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.utils.CompressionTools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * 下载服务基类（平台无关）。
 *
 * - Android：由 [DownloadService] 继承，挂载到 Service 生命周期并附加系统通知。
 * - 桌面端：由 [DownloadServiceDesktop] 继承，使用普通协程作用域与桌面通知器。
 *
 * 平台差异通过 [notifier] 与 [downloadPathProvider] 注入。
 */
abstract class BaseDownloadService(
    val scope: CoroutineScope,
) : DownloadManager.Callback {

    /** 平台提供的下载状态通知器（系统通知/桌面通知）。 */
    protected abstract val notifier: DownloadNotifier

    /** 平台提供的下载根目录。 */
    protected abstract val downloadPathProvider: DownloadPathProvider

    private var downloadManager: DownloadManager? = null
    private var audioDownloadManager: DownloadManager? = null
    private var currentTaskId = 1L
    private var idCounter = 1L

    private var audioDownloadManagerCallback = object : DownloadManager.Callback {
        override fun onTaskRunning(info: CurrentDownloadInfo) {
        }

        override fun onTaskComplete(info: CurrentDownloadInfo) {
            if (downloadManager?.downloadInfo?.status == CurrentDownloadInfo.STATUS_COMPLETED) {
                notifier.showCompletedStatusNotify(info)
                completeDownload()
            }
        }

        override fun onTaskError(info: CurrentDownloadInfo, error: Throwable) {
            // 主视频流已完成时不重置状态
        }
    }

    var downloadList = mutableListOf<BiliDownloadEntryAndPathInfo>()
    var downloadListVersion = MutableStateFlow(0)
    var waitDownloadQueue = mutableListOf<BiliDownloadEntryAndPathInfo>()
    val curDownload = MutableStateFlow<CurrentDownloadInfo?>(null)
    private val curBiliDownloadEntryAndPathInfo: BiliDownloadEntryAndPathInfo?
        get() = curDownload.value?.let { cur ->
            downloadList.find { it.entry.key == cur.id }
        }
    private var curMediaFile: File? = null
    private var curMediaFileInfo: BiliDownloadMediaFileInfo? = null

    /**
     * 服务初始化入口，由子类在其生命周期/启动时调用一次。
     */
    fun initialize() {
        scope.launch {
            readDownloadList()
        }
        scope.launch {
            curDownload.collect { info ->
                if (info == null) {
                    notifier.cancel()
                } else {
                    notifier.notifyData(info)
                }
            }
        }
    }

    fun readDownloadList() {
        val downloadDir = File(getDownloadPath())
        if (!downloadDir.exists()) {
            downloadDir.mkdir()
        }
        val list = mutableListOf<BiliDownloadEntryAndPathInfo>()
        downloadDir.listFiles()
            ?.filter { it.isDirectory }
            ?.forEach {
                list.addAll(readDownloadDirectory(it))
            }
        downloadList = list.reversed().toMutableList()
        // 通知 UI 列表已更新
        downloadListVersion.value++
    }

    fun readDownloadDirectory(dir: File): List<BiliDownloadEntryAndPathInfo> {
        if (!dir.exists() || !dir.isDirectory) {
            return emptyList()
        }
        return dir.listFiles()
            ?.filter { pageDir -> pageDir.isDirectory }
            ?.map { File(it.path, "entry.json") }
            ?.filter { it.exists() }
            ?.map {
                val entryJson = it.readText()
                val entry = MiaoJson.fromJson<BiliDownloadEntryInfo>(entryJson)
                BiliDownloadEntryAndPathInfo(
                    entry = entry,
                    entryDirPath = it.parent,
                    pageDirPath = it.parentFile.parent
                )
            } ?: emptyList()
    }

    /**
     * 是否处于等待下载队列中
     */
    fun isInWaitDownloadQueue(dirPath: String): Boolean {
        return waitDownloadQueue.indexOfFirst { it.entryDirPath == dirPath } > 0
    }

    /**
     * 创建任务
     */
    fun createDownload(
        biliEntry: BiliDownloadEntryInfo
    ) {
        val entryDir = getDownloadFileDir(biliEntry)
        // 保存视频信息
        val entryJsonFile = File(entryDir, "entry.json")
        val entryJsonStr = MiaoJson.toJson(biliEntry)
        entryJsonFile.writeText(entryJsonStr)
        val biliDownInfo = BiliDownloadEntryAndPathInfo(
            entry = biliEntry,
            pageDirPath = entryDir.parent,
            entryDirPath = entryDir.absolutePath,
        )
        val index = downloadList.indexOfFirst {
            if (biliEntry.avid != null) {
                biliEntry.avid == it.entry.avid
            } else {
                biliEntry.season_id == it.entry.season_id
            }
        }
        downloadList.add(index + 1, biliDownInfo)
        downloadListVersion.value++
        if (curDownload.value == null) {
            startDownload(biliDownInfo)
        } else {
            waitDownloadQueue.add(biliDownInfo)
        }
    }

    fun startDownload(entryDirPath: String) {
        val biliDownInfo = downloadList.find {
            it.entryDirPath == entryDirPath
        }
        if (biliDownInfo != null) {
            startDownload(biliDownInfo)
        }
    }

    /**
     * 开始任务
     */
    fun startDownload(biliDownInfo: BiliDownloadEntryAndPathInfo) = scope.launch {
        // 取消当前任务
        downloadManager?.cancel()
        audioDownloadManager?.cancel()
        downloadManager = null
        audioDownloadManager = null
        // 开始任务/继续任务
        val entryDir = File(biliDownInfo.entryDirPath)
        val danmakuXMLFile = File(entryDir, "danmaku.xml")
        val entry = biliDownInfo.entry
        val parentId = entry.season_id ?: entry.avid?.toString() ?: ""
        val id = entry.page_data?.cid ?: entry.source?.cid ?: 0L
        currentTaskId = idCounter++
        val currentDownloadInfo = CurrentDownloadInfo(
            taskId = currentTaskId,
            parentDirPath = entryDir.parent,
            parentId = parentId,
            id = id,
            name = entry.name,
            url = "",
            header = mapOf(),
            size = entry.total_bytes,
            progress = entry.downloaded_bytes,
            length = entry.total_time_milli,
        )
        if (!danmakuXMLFile.exists()) {
            try {
                // 获取弹幕并下载
                curDownload.value = currentDownloadInfo.copy(
                    status = CurrentDownloadInfo.STATUS_GET_DANMAKU,
                )
                val res = MiaoHttp.request {
                    url = BiliPalyUrlHelper.danmakuXMLUrl(biliDownInfo.entry)
                }.awaitCall()
                val xmlBytes = CompressionTools.decompressXML(res.body!!.bytes())
                danmakuXMLFile.writeBytes(xmlBytes)
            } catch (e: Exception) {
                curDownload.value = currentDownloadInfo.copy(
                    status = CurrentDownloadInfo.STATUS_FAIL_DANMAKU,
                )
                e.printStackTrace()
                return@launch
            }
        }
        downloadVideo(currentDownloadInfo, biliDownInfo)
    }

    private suspend fun downloadVideo(
        currentDownloadInfo: CurrentDownloadInfo,
        biliDownInfo: BiliDownloadEntryAndPathInfo,
    ) {
        if (currentDownloadInfo.taskId != currentTaskId) {
            return
        }
        val entry = biliDownInfo.entry
        val entryDir = File(biliDownInfo.entryDirPath)
        val videoDir = File(entryDir, entry.type_tag)
        if (!videoDir.exists()) {
            videoDir.mkdir()
        }
        try {
            curDownload.value = currentDownloadInfo.copy(
                status = CurrentDownloadInfo.STATUS_GET_PLAYURL,
            )
            //获取播放地址并下载
            val mediaFileInfo = BiliPalyUrlHelper.playUrl(entry)
            val httpHeader = mediaFileInfo.httpHeader()
            val mediaJsonFile = File(videoDir, "index.json")
            val mediaJsonStr = MiaoJson.toJson(mediaFileInfo)
            mediaJsonFile.writeText(mediaJsonStr)

            if (currentDownloadInfo.taskId != currentTaskId) {
                return
            }

            curMediaFile = mediaJsonFile
            curMediaFileInfo = mediaFileInfo
            when (mediaFileInfo) {
                is BiliDownloadMediaFileInfo.Type1 -> {
                    // TODO: 多视频文件下载
                    downloadManager = DownloadManager(scope, currentDownloadInfo.copy(
                        url = mediaFileInfo.segment_list[0].url,
                        header = httpHeader,
                        size = mediaFileInfo.segment_list[0].bytes,
                        length = mediaFileInfo.segment_list[0].duration
                    ), this).also {
                        it.start(File(videoDir, "0" + "." + mediaFileInfo.format))
                    }
                    curDownload.value = currentDownloadInfo
                }
                is BiliDownloadMediaFileInfo.Type2 -> {
                    downloadManager = DownloadManager(scope, currentDownloadInfo.copy(
                        url = mediaFileInfo.video[0].base_url,
                        header = httpHeader,
                        size = entry.total_bytes,
                        length = mediaFileInfo.duration
                    ), this)
                    curDownload.value = currentDownloadInfo
                    downloadManager?.start(File(videoDir, "video.m4s"))
                    val audio = mediaFileInfo.audio
                    if (audio != null && audio.isNotEmpty()) {
                        audioDownloadManager = DownloadManager(scope, CurrentDownloadInfo(
                            taskId = currentDownloadInfo.taskId,
                            parentDirPath = currentDownloadInfo.parentDirPath,
                            parentId = currentDownloadInfo.parentId,
                            id = currentDownloadInfo.id,
                            name = entry.name,
                            url = audio[0].base_url,
                            header = httpHeader,
                            size = audio[0].size,
                            length = mediaFileInfo.duration
                        ), audioDownloadManagerCallback)
                        audioDownloadManager?.start(File(videoDir, "audio.m4s"))
                    }
                    entry.page_data?.let {
                        entry.page_data = it.copy(
                            height = mediaFileInfo.video[0].height,
                            width = mediaFileInfo.video[0].width,
                        )
                    }
                    entry.ep?.let {
                        entry.ep = it.copy(
                            height = mediaFileInfo.video[0].height,
                            width = mediaFileInfo.video[0].width,
                        )
                    }
                    updateBiliDownloadEntryJson(biliDownInfo.entryDirPath, entry)
                }
                else -> {

                }
            }
        } catch (e: Exception) {
            curDownload.value = currentDownloadInfo.copy(
                status = CurrentDownloadInfo.STATUS_FAIL_PLAYURL,
            )
            e.printStackTrace()
        }
    }

    fun cancelDownload(taskId: Long) {
        if (taskId == currentTaskId) {
            downloadManager?.cancel()
            audioDownloadManager?.cancel()
            downloadManager = null
            audioDownloadManager = null
            currentTaskId = 0L
            stopDownload()
        }
    }

    /**
     * 结束当前任务
     */
    fun stopDownload() {
        curDownload.value?.let { cur ->
            val entryAndPathInfo = downloadList.find {
                cur.id == it.entry.key
            }
            if (entryAndPathInfo != null) {
                entryAndPathInfo.entry.total_bytes = cur.size
                entryAndPathInfo.entry.downloaded_bytes = cur.progress
                updateBiliDownloadEntryJson(
                    entryAndPathInfo.entryDirPath,
                    entryAndPathInfo.entry,
                )
                downloadListVersion.value++
                downloadManager?.cancel()
            }
        }
        curDownload.value = null
        curMediaFile = null
        curMediaFileInfo = null
        nextDownload()
    }

    /**
     * 删除当前任务
     */
    fun deleteDownload(
        pageDirPath: String,
        entryDirPath: String,
    ) {
        val index = downloadList.indexOfFirst {
            it.pageDirPath == pageDirPath && it.entryDirPath == entryDirPath
        }
        if (index != -1) {
            // 如果为当前下载任务则先停止任务
            val entryAndPathInfo = downloadList[index]
            if (curDownload.value?.id == entryAndPathInfo.entry.key) {
                cancelDownload(currentTaskId)
            }
        }
        val downloadDir = File(pageDirPath)
        if (downloadDir.exists()) {
            val entryDir = File(entryDirPath)
            if (entryDir.exists()) {
                entryDir.deleteRecursively()
            }
            if (downloadDir.listFiles()?.size == 0) {
                downloadDir.delete()
            }
        }
        if (index != -1) {
            // 从列表移除
            downloadList.removeAt(index)
            downloadListVersion.value++
        }
    }

    /**
     * 完成下载
     */
    private fun completeDownload() {
        val (_, entryDirPath, entry) = curBiliDownloadEntryAndPathInfo ?: return
        entry.downloaded_bytes = entry.total_bytes
        entry.total_bytes = entry.total_bytes
        entry.is_completed = true
        entry.total_time_milli = (curDownload.value?.length ?: 0L) * 1000
        updateBiliDownloadEntryJson(entryDirPath, entry)
        downloadListVersion.value++
        curDownload.value = null
        curMediaFile = null
        curMediaFileInfo = null
        downloadManager = null
        audioDownloadManager = null
        nextDownload()
    }

    /**
     * 完成下载
     */
    private fun nextDownload() {
        if (waitDownloadQueue.isNotEmpty()) {
            val next = waitDownloadQueue[0]
            waitDownloadQueue.removeAt(0)
            if (downloadList.indexOfFirst { it.entry.key == next.entry.key } != -1) {
                startDownload(next)
            } else {
                nextDownload()
            }
        }
    }

    override fun onTaskRunning(info: CurrentDownloadInfo) {
        // 获取视频文件长度
        if (info.progress == 0L && info.size != 0L) {
            (curMediaFileInfo as BiliDownloadMediaFileInfo.Type2)?.let {
                if (it.video[0].size == 0L && info.size != 0L) {
                    it.video[0].size = info.size
                    val mediaJsonStr = MiaoJson.toJson(it)
                    curMediaFile?.writeText(mediaJsonStr)
                }
            }
            val entryAndPathInfo = downloadList.find {
                info.id == it.entry.key
            }
            if (entryAndPathInfo != null) {
                entryAndPathInfo.entry.total_bytes = info.size
                updateBiliDownloadEntryJson(
                    entryAndPathInfo.entryDirPath,
                    entryAndPathInfo.entry,
                )
                downloadListVersion.value++
            }
        }
        curDownload.value = info.copy()
    }

    override fun onTaskComplete(info: CurrentDownloadInfo) {
        if (info.size == 0L || info.size != info.progress) {
            // TODO: 未知错误
            return
        }
        when (audioDownloadManager?.downloadInfo?.status) {
            CurrentDownloadInfo.STATUS_DOWNLOADING -> {
                // 等待音频下载完成
                curDownload.value = info.copy(
                    status = CurrentDownloadInfo.STATUS_AUDIO_DOWNLOADING
                )
            }
            CurrentDownloadInfo.STATUS_FAIL_DOWNLOAD -> {
                // 重新下载音频
                curBiliDownloadEntryAndPathInfo?.let(::startDownload)
            }
            CurrentDownloadInfo.STATUS_COMPLETED, null -> {
                // 完成下载
                notifier.showCompletedStatusNotify(info)
                completeDownload()
            }
        }
    }

    override fun onTaskError(info: CurrentDownloadInfo, error: Throwable) {
        error.printStackTrace()
        curDownload.value = info.copy(
            status = CurrentDownloadInfo.STATUS_FAIL_DOWNLOAD
        )
        notifier.showErrorStatusNotify(info)
        val entryAndPathInfo = downloadList.find {
            info.id == it.entry.key
        }
        if (entryAndPathInfo != null) {
            entryAndPathInfo.entry.total_bytes = info.size
            entryAndPathInfo.entry.downloaded_bytes = info.progress
            updateBiliDownloadEntryJson(
                entryAndPathInfo.entryDirPath,
                entryAndPathInfo.entry,
            )
            downloadListVersion.value++
        }
    }

    private fun updateBiliDownloadEntryJson(
        entryDirPath: String,
        entry: BiliDownloadEntryInfo,
    ) {
        // 保存视频信息
        val entryJsonFile = File(entryDirPath, "entry.json")
        val entryJsonStr = MiaoJson.toJson(entry)
        entryJsonFile.writeText(entryJsonStr)
    }

    fun getDownloadPath(): String = downloadPathProvider.downloadPath

    private fun getDownloadFileDir(biliEntry: BiliDownloadEntryInfo): File {
        var dirName = ""
        var pageDirName = ""
        val ep = biliEntry.ep
        if (ep != null) {
            dirName = "s_" + biliEntry.season_id!!
            pageDirName = ep.episode_id.toString()
        }
        val page = biliEntry.page_data
        if (page != null) {
            dirName = biliEntry.avid!!.toString()
            pageDirName = "c_" + page.cid
        }
        val downloadDir = File(getDownloadPath(), dirName)
        // 创建文件夹
        if (!downloadDir.exists()) {
            downloadDir.mkdir()
        }
        val pageDir = File(downloadDir, pageDirName)
        if (!pageDir.exists()) {
            pageDir.mkdir()
        }
        return pageDir
    }
}

/**
 * 下载目录提供者。
 *
 * Android：使用应用专属存储下的 `download` 子目录。
 * 桌面端：使用平台数据目录下的 `download` 子目录。
 */
interface DownloadPathProvider {
    val downloadPath: String
}

/**
 * 下载状态通知器抽象，由平台实现（系统通知 / 桌面通知）。
 */
interface DownloadNotifier {
    /** 更新进行中任务的状态。 */
    fun notifyData(info: CurrentDownloadInfo)
    /** 显示下载完成通知。 */
    fun showCompletedStatusNotify(info: CurrentDownloadInfo)
    /** 显示下载出错通知。 */
    fun showErrorStatusNotify(info: CurrentDownloadInfo)
    /** 取消进行中任务的显示。 */
    fun cancel()
}

/**
 * 无操作的通知器，桌面端可作为默认实现，
 * 状态仍可通过 [BaseDownloadService.curDownload] StateFlow 获取。
 */
object NoopDownloadNotifier : DownloadNotifier {
    override fun notifyData(info: CurrentDownloadInfo) {}
    override fun showCompletedStatusNotify(info: CurrentDownloadInfo) {}
    override fun showErrorStatusNotify(info: CurrentDownloadInfo) {}
    override fun cancel() {}
}
