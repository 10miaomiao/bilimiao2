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
import android.net.Uri
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import com.a10miaomiao.bilimiao.MainActivity
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.service.PlayerService
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PlayingNotification(
    val playerService: PlayerService,
    val scope: CoroutineScope,
) {

    companion object {

        const val ACTION_CMD_TOGGLE_PAUSE = "${PlayerService.BILIMIAO_PLAYER_PACKAGE_NAME}.PlayingNotification.cmd.togglepause"
        const val ACTION_CMD_CLOSE = "${PlayerService.BILIMIAO_PLAYER_PACKAGE_NAME}.PlayingNotification.cmd.close"

        const val MEDIA_SESSION_ACTIONS = (PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_SEEK_TO)

        const val NOTIFICATION_CONTROLS_SIZE_MULTIPLIER = 1.0f
        internal const val NOTIFICATION_CHANNEL_ID = "${PlayerService.BILIMIAO_PLAYER_PACKAGE_NAME}.playing_notification"
        internal const val NOTIFICATION_CHANNEL_NAME = "playing_notification"

        const val NOTIFICATION_ID = 10071

        private const val NOTIFICATION_LARGE_ICON_SIZE = 144 // px

        private val glideOptions = RequestOptions()
            .fallback(R.drawable.top_bg1)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    }

    val manager: NotificationManager = playerService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var notificationBuilder: NotificationCompat.Builder? = null
    private var mediaSessionToken: MediaSessionCompat.Token? = null
    private var playingInfo = PlayerService.PlayingInfo()
    private var currentCoverUrl: String = ""
    private var currentCoverBitmap: Bitmap? = null

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

    fun cancel() {
        playingInfo = PlayerService.PlayingInfo()
        currentCoverUrl = ""
        currentCoverBitmap = null
        notificationBuilder = null
        manager.cancel(NOTIFICATION_ID)
    }

    fun setPlayingInfo(
        mediaSession: MediaSessionCompat?,
        info: PlayerService.PlayingInfo
    ) {
        playingInfo = info
        val coverUrl = info.cover ?: ""
        val sessionToken = mediaSession?.sessionToken ?: return
        val builder = getNotificationBuilder(sessionToken)
        setNotificationActions(builder)
        if (coverUrl != currentCoverUrl && coverUrl.isNotBlank()) {
            currentCoverBitmap = null
            currentCoverUrl = coverUrl
            scope.launch {
                val uri = Uri.parse(UrlUtil.autoHttps(coverUrl))
                val coverBitmap = resolveUriAsBitmap(uri)
                if (coverUrl == currentCoverUrl && coverBitmap != null) {
                    currentCoverBitmap = coverBitmap
                    updateWithBitmap(coverBitmap)
                    val metaData = playerService.getMediaMetadata(info)
                    metaData.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, coverBitmap)
                    mediaSession.setMetadata(metaData.build())
                }
            }
        }
        val notification: Notification = builder.build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun updateForPlaying(mediaSession: MediaSessionCompat?) {
        val sessionToken = mediaSession?.sessionToken ?: return
        val builder = getNotificationBuilder(sessionToken)
        setNotificationActions(builder)
        val notification: Notification = builder.build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun updateWithProgress(mediaSession: MediaSessionCompat?, max: Long, progress: Long) {
        val sessionToken = mediaSession?.sessionToken ?: return
        val builder = getNotificationBuilder(sessionToken)
        setNotificationActions(builder)
        builder.setProgress(max.toInt(), progress.toInt(), true)
        val notification: Notification = builder.build()
        manager.notify(NOTIFICATION_ID, notification)
    }


    private fun updateWithBitmap(bitmap: Bitmap) {
        val sessionToken = mediaSessionToken ?: return
        val builder = getNotificationBuilder(sessionToken)
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

    private fun getNotificationBuilder(
        sessionToken: MediaSessionCompat.Token
    ): NotificationCompat.Builder {
        if (sessionToken === mediaSessionToken) {
            notificationBuilder?.let { return it }
        }
        mediaSessionToken = sessionToken

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
        style.setMediaSession(sessionToken)
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

    private suspend fun resolveUriAsBitmap(uri: Uri): Bitmap? {
        return withContext(Dispatchers.IO) {
            // Block on downloading artwork.
            try {
                Glide.with(playerService)
                    .applyDefaultRequestOptions(glideOptions)
                    .asBitmap()
                    .load(uri)
                    .submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE)
                    .get()
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }

        }
    }
}