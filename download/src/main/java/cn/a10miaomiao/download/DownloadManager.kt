package cn.a10miaomiao.download

import android.util.Log
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import okhttp3.Call
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class DownloadManager {
    private val mClient = OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

    private var call: Call? = null
    private var disposable: Disposable? = null
    private var downloadInfo: DownloadInfo? = null
    var callback: Callback? = null

    fun start(info: DownloadInfo, file: File, downloadedLength: Long = 0) {
        downloadInfo = info
        disposable = create(info, file, downloadedLength)
                .throttleLast(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ info ->
                    callback?.onTaskRunning(info)
                }, { e ->
                    info.status = -1
                    callback?.onTaskError(info, e)
                }, {
                    info.status = 1
                    callback?.onTaskComplete(info)
                })

    }

    /**
     * 创建下载
     */
    private fun create(info: DownloadInfo, file: File, downloadedLength: Long = 0) = Observable.create<DownloadInfo> {
        if (file.exists()) {
            info.progress = file.length()
        }
        var downloadLength = info.progress //已经下载好的长度
        val request = Request.Builder()
                .url(info.url.replace("http://", "https://"))
        if (downloadLength > 0) {
            request.addHeader("RANGE", "bytes=$downloadLength-${info.size}")
        }
        downloadLength += downloadedLength
        for (keys in info.header.keys) {
            request.addHeader(keys, info.header[keys]!!)
        }
        call = mClient.newCall(request.build())
        val response = call!!.execute()
        if (!response.isSuccessful) {
            it.onError(Throwable())
            return@create
        }
        var fileOutputStream: FileOutputStream? = null
        try {
            val `is` = response.body!!.byteStream()
            val bis = BufferedInputStream(`is`)
            fileOutputStream = FileOutputStream(file, true)
            var buffer = ByteArray(2048) //缓冲数组2kB
            var len: Int = bis.read(buffer)
            while (len != -1) {
                fileOutputStream!!.write(buffer, 0, len)
                downloadLength += len
                info.progress = downloadLength
                it.onNext(info)
                len = bis.read(buffer)
            }
            fileOutputStream.flush()
            it.onComplete()
        } catch (e: IOException) {
            e.printStackTrace()
            it.onError(e)
        }
    }

    /**
     * 取消下载
     */
    fun cancel(): DownloadInfo? {
        call?.cancel()
        downloadInfo?.status = 2
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
            val call = mClient!!.newCall(request)
            var response = call.execute()
//                log("----response----")
            if (response != null && response.isSuccessful) {
                val contentLength = response.body!!.contentLength()
                call.cancel()
//                    log("contentLength", contentLength)
                return if (contentLength == 0L) -1 else contentLength
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
//            log("----读取长度失败----")
        return -1
    }

    interface Callback {
        fun onTaskRunning(info: DownloadInfo): Unit
        fun onTaskComplete(info: DownloadInfo): Unit
        fun onTaskError(info: DownloadInfo, error: Throwable): Unit
    }

}