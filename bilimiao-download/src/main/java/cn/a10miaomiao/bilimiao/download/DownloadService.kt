package cn.a10miaomiao.bilimiao.download

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryAndPathInfo
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadMediaFileInfo
import cn.a10miaomiao.bilimiao.download.entry.CurrentDownloadInfo
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.CoroutineContext

class DownloadService: Service(), CoroutineScope, DownloadManager.Callback {
    companion object {
        private const val TAG = "DownloadService"
        private val channel = Channel<DownloadService>()
        private var _instance: DownloadService? = null

        val instance get() = _instance

        suspend fun getService(context: Context): DownloadService{
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
    private var downloadNotify: DownloadNotify? = null
    private var downloadManager: DownloadManager? = null
    private var audioDownloadManager: DownloadManager? = null
    private var audioDownloadManagerCallback = object : DownloadManager.Callback {
        override fun onTaskRunning(info: CurrentDownloadInfo) {
//            TODO("Not yet implemented")
        }

        override fun onTaskComplete(info: CurrentDownloadInfo) {
//            TODO("Not yet implemented")
        }

        override fun onTaskError(info: CurrentDownloadInfo, error: Throwable) {
//            TODO("Not yet implemented")
        }

    }

    var downloadList = mutableListOf<BiliDownloadEntryAndPathInfo>()
    var downloadListVersion = MutableStateFlow(0)
    val curDownload = MutableStateFlow<CurrentDownloadInfo?>(null)
    private val curBiliDownloadEntryAndPathInfo: BiliDownloadEntryAndPathInfo?
        get() = curDownload.value?.let { cur ->
            downloadList.find { it.entry.key == cur.id }
        }
    private var curMediaFile: File? = null
    private var curMediaFileInfo: BiliDownloadMediaFileInfo? = null


    override fun onCreate() {
        super.onCreate()
        job = Job()
        launch {
            readDownloadList()
            channel.send(this@DownloadService)
        }
        downloadNotify = DownloadNotify(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        _instance = null
    }

    private fun readDownloadList() {
        val downloadDir = File(getDownloadPath())
        val list = mutableListOf<BiliDownloadEntryAndPathInfo>()
        downloadDir.listFiles()
            .filter { it.isDirectory }
            .forEach {
                list.addAll(readDownloadDirectory(it))
            }
        downloadList = list.reversed().toMutableList()
    }

    fun readDownloadDirectory(dir: File): List<BiliDownloadEntryAndPathInfo>{
        if (!dir.exists() || !dir.isDirectory) {
            return emptyList()
        }
        return dir.listFiles()
            .filter { pageDir -> pageDir.isDirectory }
            .map { File(it.path, "entry.json") }
            .filter { it.exists() }
            .map {
                val entryJson = it.readText()
                val entry = Gson().fromJson(entryJson, BiliDownloadEntryInfo::class.java)
                BiliDownloadEntryAndPathInfo(
                    entry = entry,
                    entryDirPath = it.parent,
                    pageDirPath = it.parentFile.parent
                )
            }
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
        val entryJsonStr = Gson().toJson(biliEntry)
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
        }
    }

    fun startDownload(entryDirPath: String) {
        val biliDownInfo = downloadList.find {
            it.entryDirPath == entryDirPath
        }
        if (biliDownInfo != null) {
            startDownload(biliDownInfo)
        } else {
            val entryFile = File(entryDirPath, "entry.json")
            if (entryFile.exists()) {

            }
        }
    }
    /**
     * 开始任务
     */
    fun startDownload(biliDownInfo: BiliDownloadEntryAndPathInfo) = launch {
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
        val id = entry.page_data?.cid ?: entry.ep?.episode_id ?: 0L
        val currentDownloadInfo = CurrentDownloadInfo(
            parentId = parentId,
            id = id,
            name = entry.name,
            url = "",
            header = mapOf(),
            size = 0,
            length = 0
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
                val inputStream = res.body!!.byteStream()
                inputStream.inputStreamToFile(danmakuXMLFile)
            } catch (e: Exception){
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
            val httpHeader = BiliPalyUrlHelper.httpHeader(entry)
            val mediaJsonFile = File(videoDir, "index.json")
            val mediaJsonStr = Gson().toJson(mediaFileInfo)
            mediaJsonFile.writeText(mediaJsonStr)

            curMediaFile = mediaJsonFile
            curMediaFileInfo = mediaFileInfo
            when(mediaFileInfo) {
                is BiliDownloadMediaFileInfo.Type1 -> {
                    downloadManager = DownloadManager(this, currentDownloadInfo.copy(
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
                    downloadManager = DownloadManager(this, currentDownloadInfo.copy(
                        url = mediaFileInfo.video[0].base_url,
                        header = httpHeader,
                        size = entry.total_bytes,
                        length = mediaFileInfo.duration
                    ), this)
                    downloadManager?.start(File(videoDir, "video.m4s"))
                    curDownload.value = currentDownloadInfo
                    val audio = mediaFileInfo.audio
                    if (audio != null && audio.isNotEmpty()) {
                        audioDownloadManager = DownloadManager(this, CurrentDownloadInfo(
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

    fun cancelDownload() {
        DebugMiao.log("cancelDownload", curDownload.value)
        downloadManager?.cancel()
        audioDownloadManager?.cancel()
        downloadManager = null
        audioDownloadManager = null
        stopDownload()
    }

    /**
     * 结束当前任务
     */
    fun stopDownload () {
        curDownload.value?.let { cur ->
            val entryAndPathInfo = downloadList.find {
                cur.id == it.entry.key
            }
            if (entryAndPathInfo != null) {
                entryAndPathInfo.entry.total_bytes = cur.size
                entryAndPathInfo.entry.downloaded_bytes = cur.progress
                val entryJsonFile = File(entryAndPathInfo.entryDirPath, "entry.json")
                val entryJsonStr = Gson().toJson(entryAndPathInfo.entry)
                entryJsonFile.writeText(entryJsonStr)
                downloadManager?.cancel()?.let {
                    curDownload.value = it
                }
            }
        }
        curDownload.value = null
        curMediaFile = null
        curMediaFileInfo = null
    }

    /**
     * 删除当前任务
     */
    fun deleteDownload (
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
                cancelDownload()
            }
        }
        val downloadDir = File(pageDirPath)
        if (downloadDir.exists()) {
            val entryDir = File(entryDirPath)
            if (entryDir.exists()) {
                entryDir.deleteRecursively()
            }
            if (downloadDir.listFiles().size === 0) {
                downloadDir.delete()
            }
        }
        if (index != -1) {
            // 从列表移除
            downloadList.removeAt(index)
            downloadListVersion.value++
        }
    }

    private fun nextDownload() {
        downloadList.find {
//            !it.entry.is_completed
            it.entry.total_bytes == 0L
        }?.let {
            startDownload(it)
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onTaskRunning(info: CurrentDownloadInfo) {
        // 获取视频文件长度
        if (info.progress == 0L && info.size != 0L) {
            (curMediaFileInfo as BiliDownloadMediaFileInfo.Type2)?.let {
                if (it.video[0].size == 0L && info.size != 0L) {
                    it.video[0].size = info.size
                    val mediaJsonStr = Gson().toJson(it)
                    curMediaFile?.writeText(mediaJsonStr)
                }
            }
        }
        curDownload.value = info.copy()
        downloadNotify?.notifyData(info)
    }

    override fun onTaskComplete(info: CurrentDownloadInfo) {
        if (info.size != 0L && info.size == info.progress) {
            val (_, entryDirPath, entry) = curBiliDownloadEntryAndPathInfo ?: return
            entry.downloaded_bytes = info.progress
            entry.total_bytes = info.size
            entry.is_completed = true
            val entryJsonFile = File(entryDirPath, "entry.json")
            val entryJsonStr = Gson().toJson(entry)
            entryJsonFile.writeText(entryJsonStr)
//            downloadList.removeAt(index)
            downloadListVersion.value++
            curDownload.value = null
            curMediaFile = null
            curMediaFileInfo = null
        } else {

        }
        // TODO: 视频下载完成，但音频未完成的情况
        downloadNotify?.notifyData(info)
        nextDownload()
    }

    override fun onTaskError(info: CurrentDownloadInfo, error: Throwable) {
        DebugMiao.log(TAG, "onTaskError", info)
        error.printStackTrace()
        downloadNotify?.notifyData(info)
    }

    private fun getDownloadPath(): String {
        var file = File(getExternalFilesDir(null), "../download")
        if (!file.exists()) {
            file.mkdir()
        }
        return file.canonicalPath
    }

    private fun getDownloadFileDir(biliEntry: BiliDownloadEntryInfo): File {
        var dirName = ""
        var pageDirName = ""
        val ep = biliEntry.ep
        if (ep != null) {
            dirName = biliEntry.season_id!!
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

    @Throws(IOException::class)
    private fun InputStream.inputStreamToFile(file: File) {
        val outputStream = FileOutputStream(file)
        var read = -1
        outputStream.use {
            while (read().also { read = it } != -1) {
                it.write(read)
            }
        }
    }

}