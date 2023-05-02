package com.a10miaomiao.bilimiao.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.session.PlaybackState
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.a10miaomiao.bilimiao.MainActivity
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.service.PlayerService
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target


class PlayingNotification(
    val playerService: PlayerService,
) {

    companion object {

        const val ACTION_CMD_TOGGLE_PAUSE = "${PlayerService.BILIMIAO_PLAYER_PACKAGE_NAME}.PlayingNotification.cmd.togglepause"
        const val ACTION_CMD_CLOSE = "${PlayerService.BILIMIAO_PLAYER_PACKAGE_NAME}.PlayingNotification.cmd.close"

        const val MEDIA_SESSION_ACTIONS = (PlaybackState.ACTION_PLAY
                or PlaybackState.ACTION_PAUSE
                or PlaybackState.ACTION_PLAY_PAUSE
                or PlaybackState.ACTION_SKIP_TO_NEXT
                or PlaybackState.ACTION_SKIP_TO_PREVIOUS
                or PlaybackState.ACTION_STOP
                or PlaybackState.ACTION_SEEK_TO)

        const val NOTIFICATION_CONTROLS_SIZE_MULTIPLIER = 1.0f
        internal const val NOTIFICATION_CHANNEL_ID = "${PlayerService.BILIMIAO_PLAYER_PACKAGE_NAME}.playing_notification"
        internal const val NOTIFICATION_CHANNEL_NAME = "playing_notification"

        const val NOTIFICATION_ID = 10071
    }

    val manager: NotificationManager = playerService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var notificationBuilder: NotificationCompat.Builder? = null
    private var playingInfo = PlayerService.PlayingInfo()
    private var coverBitmap: Bitmap? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            mChannel.enableLights(false)
            mChannel.enableVibration(false)
            mChannel.setShowBadge(false)
            manager.createNotificationChannel(mChannel)
        }
    }

    private fun getDefaultImageBitmap(): Bitmap {
        return BitmapFactory.decodeResource(
            playerService.resources,
            R.drawable.top_bg1
        )
    }

    fun cancel() {
        playingInfo = PlayerService.PlayingInfo()
        coverBitmap = null
        notificationBuilder = null
        manager.cancel(NOTIFICATION_ID)
    }

    fun setPlayingInfo(info: PlayerService.PlayingInfo) {
        playingInfo = info
        val url = playingInfo.cover
        if (url == null){
            coverBitmap = null
        } else {
            val newUrl = if ("://" in url) {
                url.replace("http://","https://")
            } else { "https:$url" }
            val bigNotificationImageSize = playerService.resources
                .getDimensionPixelSize(R.dimen.notification_big_image_size)
            Glide.with(playerService)
                .asBitmap()
                .centerCrop()
                .override(bigNotificationImageSize)
                .load(newUrl)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        val defaultCoverBitmap = getDefaultImageBitmap()
                        updateWithBitmap(defaultCoverBitmap)
                        return true
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (resource != null) {
                            updateWithBitmap(resource)
                        } else {
                            val defaultCoverBitmap = getDefaultImageBitmap()
                            updateWithBitmap(defaultCoverBitmap)
                        }
                        return true
                    }
                })
                .submit()
        }
    }

    fun updateForPlaying() {
        val builder = getNotificationBuilder()
//        builder.setLargeIcon(bitmap)
        val notification: Notification = builder.build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun updateWithProgress(max: Long, progress: Long) {
        val builder = getNotificationBuilder()
        builder.setProgress(max.toInt(), progress.toInt(), true)
        val notification: Notification = builder.build()
        manager.notify(NOTIFICATION_ID, notification)
    }


    private fun updateWithBitmap(bitmap: Bitmap) {
        val builder = getNotificationBuilder()
        builder.setLargeIcon(bitmap)
        val notification: Notification = builder.build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun setNotificationActions(builder: NotificationCompat.Builder) {
        val playPauseIcon: Int = if (playerService.isPlaying()) {
            R.drawable.ic_pause_white_48dp
        } else { R.drawable.ic_play_arrow_white_48dp }
        builder.clearActions()
        builder.addAction(
            playPauseIcon,
            "Play/Pause",
            getControlIntent(ACTION_CMD_TOGGLE_PAUSE),
        )
        builder.addAction(
            R.drawable.ic_close_white_24dp,
            "Close",
            getControlIntent(ACTION_CMD_CLOSE)
        )
    }

    private fun getNotificationBuilder(): NotificationCompat.Builder {
        notificationBuilder?.let {
            setNotificationActions(it)
            return it
        }
        val builder = NotificationCompat.Builder(playerService, NOTIFICATION_CHANNEL_ID)

        builder.setContentTitle(playingInfo.title ?: "bilimiao正在播放")
        builder.setContentText(playingInfo.author ?: "bilimiao")
        builder.setSmallIcon(R.drawable.bili_default_image_tv)
//        builder.setLargeIcon(bitmap)
//        builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        builder.setShowWhen(false)
//        builder.setOngoing(playerService.isPlaying())
        builder.setOngoing(true)
        builder.setProgress(1000, 500, true)
        builder.priority = NotificationManager.IMPORTANCE_HIGH

        val intent = Intent(playerService, MainActivity::class.java)
        val pIntent: PendingIntent = PendingIntent.getActivity(
            playerService,
            1,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        builder.setContentIntent(pIntent)

        val style = androidx.media.app.NotificationCompat.MediaStyle()
        style.setMediaSession(playerService.mediaSession.sessionToken)
        //CancelButton在5.0以下的机器有效
        style.setCancelButtonIntent(pIntent)
        style.setShowCancelButton(true)
        //设置要现实在通知右方的图标 最多三个
        style.setShowActionsInCompactView(0, 1)
        style.setBuilder(builder)
        builder.setStyle(style)
        setNotificationActions(builder)
        notificationBuilder = builder
        return builder
    }

    private fun getControlIntent(action: String): PendingIntent {
        val serviceName = ComponentName(playerService, PlayerService::class.java)
        val intent = Intent(action)
        intent.component = serviceName
        return PendingIntent.getService(
            playerService,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else { 0 }
        )
    }

}