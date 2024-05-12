package cn.a10miaomiao.bilimiao.download

import cn.a10miaomiao.bilimiao.download.entry.CurrentDownloadInfo
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class DownloadManager(
    val scope: CoroutineScope,
    val downloadInfo: CurrentDownloadInfo,
    val callback: Callback,
) {

    private val mClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    fun start(file: File, downloadedLength: Long = 0) {
        scope.launch {
            create(downloadInfo, file, downloadedLength).run {
                throttleFirst(200)
            }.catch { e ->
                downloadInfo.status = CurrentDownloadInfo.STATUS_FAIL_DOWNLOAD
                callback.onTaskError(downloadInfo, e)
            }.onCompletion {
                if (downloadInfo.status == CurrentDownloadInfo.STATUS_COMPLETED) {
                    callback.onTaskComplete(downloadInfo)
                }
            }.collect {
                if (it.status == CurrentDownloadInfo.STATUS_DOWNLOADING) {
                    callback.onTaskRunning(it)
                }
            }
        }
    }

    private fun <T> Flow<T>.throttleFirst(periodMillis: Long): Flow<T> {
        return flow {
            var lastTime = 0L
            collect { value ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTime >= periodMillis) {
                    lastTime = currentTime
                    emit(value)
                }
            }
        }
    }

    /**
     * 创建下载
     */
    private fun create(info: CurrentDownloadInfo, file: File, downloadedLength: Long = 0) = flow<CurrentDownloadInfo> {
        if (file.exists()) {
            if (info.size == 0L) {
                file.delete()
            } else {
                info.progress = file.length()
            }
        }
        var downloadLength = info.progress //已经下载好的长度
        val request = Request.Builder()
            .url(UrlUtil.autoHttps(info.url))
        if (downloadLength > 0 && info.size != 0L) {
            if (info.size == downloadLength) {
                downloadInfo.status = CurrentDownloadInfo.STATUS_COMPLETED
                return@flow
            }
            request.addHeader("RANGE", "bytes=$downloadLength-${info.size}")
        }
        downloadLength += downloadedLength
        for (keys in info.header.keys) {
            request.addHeader(keys, info.header[keys]!!)
        }
        val call = mClient.newCall(request.build())
        val response = call!!.execute()
        if (!response.isSuccessful) {
            throw Throwable()
        }
        downloadInfo.status = CurrentDownloadInfo.STATUS_DOWNLOADING
        var fileOutputStream: FileOutputStream? = null
        val body = response.body!!
        if (info.size == 0L) {
            info.size = body.contentLength()
            emit(info)
        }
        val `is` = response.body!!.byteStream()
        val bis = BufferedInputStream(`is`)
        fileOutputStream = FileOutputStream(file, true)
        var buffer = ByteArray(2048) //缓冲数组2kB
        var len: Int = bis.read(buffer)
        while (len != -1 && downloadInfo.status == CurrentDownloadInfo.STATUS_DOWNLOADING) {
            fileOutputStream.write(buffer, 0, len)
            downloadLength += len
            info.progress = downloadLength
            emit(info)
            len = bis.read(buffer)
        }
        if (downloadInfo.status == CurrentDownloadInfo.STATUS_PAUSE) {
            call.cancel()
        } else {
            downloadInfo.status = CurrentDownloadInfo.STATUS_COMPLETED
        }
        fileOutputStream.flush()
    }

    /**
     * 取消下载
     */
    fun cancel(): CurrentDownloadInfo? {
        downloadInfo.status = CurrentDownloadInfo.STATUS_PAUSE
        return downloadInfo
    }

    /**
     * 获取下载长度
     *
     * @param downloadUrl
     * @return
     */
    private fun getContentLength(downloadUrl: String): Long {
        val request = Request.Builder()
            .url(downloadUrl)
            .build()
        try {
            val call = mClient.newCall(request)
            var response = call.execute()
            if (response != null && response.isSuccessful) {
                val contentLength = response.body!!.contentLength()
                call.cancel()
                return if (contentLength == 0L) -1 else contentLength
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return -1
    }

    interface Callback {
        fun onTaskRunning(info: CurrentDownloadInfo)
        fun onTaskComplete(info: CurrentDownloadInfo)
        fun onTaskError(info: CurrentDownloadInfo, error: Throwable)
    }
}