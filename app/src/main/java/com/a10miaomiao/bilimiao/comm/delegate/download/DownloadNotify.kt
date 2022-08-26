package com.a10miaomiao.bilimiao.comm.delegate.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import cn.a10miaomiao.download.DownloadInfo

class DownloadNotify(val activity: AppCompatActivity) {
    val ACTION_CMD = "com.a10miaomiao.bilimiao.DownloadNotify"
    val notificationID = 23333
    val channelId = "com.a10miaomiao.bilimiao.DownloadNotify.control"
    val manager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(channelId, "DownloadControl", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(mChannel)
        }
    }

    fun notifyData(info: DownloadInfo) {
        val builder = NotificationCompat.Builder(activity, channelId)
        builder.setContentTitle(info.name)
        val subText = info.statusText
        builder.setContentText(subText)
        builder.setSmallIcon(android.R.drawable.stat_sys_download)
        builder.setProgress(info.size.toInt(), info.progress.toInt(), false)
        builder.setOngoing(true)
        val notification = builder.build()
        manager.notify(notificationID, notification)
    }

    fun cancel() {
        manager.cancel(notificationID)
    }

}