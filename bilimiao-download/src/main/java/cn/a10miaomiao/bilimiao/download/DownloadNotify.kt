package cn.a10miaomiao.bilimiao.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import cn.a10miaomiao.bilimiao.download.entry.CurrentDownloadInfo
import okhttp3.internal.notify

class DownloadNotify(val context: Context) {
    val ACTION_CMD = "cn.a10miaomiao.bilimiao.download.DownloadNotify"
    val notificationID = 10000
    val channelId = "cn.a10miaomiao.bilimiao.download.DownloadNotify.control"
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val builder = NotificationBuilder(context, channelId).apply {
        val pageUrl = "download/list"
        val uri = Uri.parse("bilimiao://compose?url=${Uri.encode(pageUrl)}")
        val pendingIntent = getPendingIntent(uri)
        setContentIntent(pendingIntent)
        setSmallIcon(android.R.drawable.stat_sys_download)
        priority = NotificationCompat.PRIORITY_DEFAULT
        setOnlyAlertOnce(true)
        setOngoing(true)
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(channelId, "DownloadControl", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(mChannel)
        }
    }

    fun notifyData(info: CurrentDownloadInfo) {
        if (builder.taskId == info.taskId) {
            builder.setContentText(info.statusText)
            builder.setProgress(info.size.toInt(), info.progress.toInt(), false)
        } else {
            builder.setContentTitle(info.name)
            builder.setContentText(info.statusText)
            builder.setProgress(info.size.toInt(), info.progress.toInt(), false)
            builder.taskId = info.taskId
        }
        val notification = builder.build()
        manager.notify(notificationID, notification)
    }

    fun showCompletedStatusNotify(info: CurrentDownloadInfo) {
        manager.notify(
            notificationID + info.taskId.toInt(),
            NotificationCompat.Builder(context, channelId).apply {
                val pageUrl = "download/detail?path=${info.parentDirPath}"
                val uri = Uri.parse("bilimiao://compose?url=${Uri.encode(pageUrl)}")
                val pendingIntent = getPendingIntent(uri)
                setContentIntent(pendingIntent)
                setContentTitle(info.name)
                setContentText("下载完成")
                setSmallIcon(R.drawable.ic_baseline_file_download_done_24)
            }.build()
        )
    }

    fun showErrorStatusNotify(info: CurrentDownloadInfo) {
        manager.notify(
            notificationID + info.taskId.toInt(),
            NotificationCompat.Builder(context, channelId).apply {
                val pageUrl = "download/detail?path=${info.parentDirPath}"
                val uri = Uri.parse("bilimiao://compose?url=${Uri.encode(pageUrl)}")
                val pendingIntent = getPendingIntent(uri)
                setContentIntent(pendingIntent)
                setContentTitle(info.name)
                setContentText("下载出错")
                setSmallIcon(R.drawable.ic_baseline_error_24)
            }.build()
        )
    }

    fun cancel() {
        manager.cancel(notificationID)
    }

    private fun getPendingIntent(
        uri: Uri,
    ): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW).also {
            it.data = uri
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(context, 0, intent, 0)
        }
    }

    class NotificationBuilder(
        context: Context,
        channelId: String,
    ) : NotificationCompat.Builder(context, channelId) {
        var taskId = 0L
    }

}