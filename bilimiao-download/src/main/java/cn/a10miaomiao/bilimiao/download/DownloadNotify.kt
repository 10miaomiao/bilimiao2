package cn.a10miaomiao.bilimiao.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import cn.a10miaomiao.bilimiao.download.entry.CurrentDownloadInfo

class DownloadNotify(val context: Context) {
    val ACTION_CMD = "cn.a10miaomiao.bilimiao.download.DownloadNotify"
    val notificationID = 1010
    val channelId = "cn.a10miaomiao.bilimiao.download.DownloadNotify.control"
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val builder = NotificationBuilder(context, channelId)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(channelId, "DownloadControl", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(mChannel)
        }
    }

    fun notifyData(info: CurrentDownloadInfo) {
//        val builder = NotificationCompat.Builder(context, channelId)
        if (builder.info?.id == info.id
            && builder.info?.parentId == info.parentId) {
            builder.setContentText(info.statusText)
            builder.setProgress(info.size.toInt(), info.progress.toInt(), false)
        } else {
            builder.setContentTitle(info.name)
            builder.setContentText(info.statusText)
            builder.setSmallIcon(android.R.drawable.stat_sys_download)
            builder.setProgress(info.size.toInt(), info.progress.toInt(), false)
            builder.priority = NotificationCompat.PRIORITY_DEFAULT
            builder.setOnlyAlertOnce(true)
            builder.setOngoing(true)
            builder.info = info
        }
        val notification = builder.build()
        manager.notify(notificationID, notification)
    }

    fun cancel() {
        manager.cancel(notificationID)
    }

    class NotificationBuilder(
        context: Context,
        channelId: String,
    ) : NotificationCompat.Builder(context, channelId) {
        var info: CurrentDownloadInfo? = null
    }

}