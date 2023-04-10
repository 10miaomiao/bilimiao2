package com.a10miaomiao.bilimiao.service

import android.app.Service
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.a10miaomiao.bilimiao.page.setting.VideoSettingFragment
import com.a10miaomiao.bilimiao.service.notification.PlayingNotification
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer

class PlayerService : Service() {

    companion object {
        val TAG: String = PlayerService::class.java.simpleName
        var selfInstance: PlayerService? = null
            private set

        const val BILIMIAO_PLAYER_PACKAGE_NAME = "com.a10miaomiao.bilimiao.player"
        const val ACTION_CREATED = "$BILIMIAO_PLAYER_PACKAGE_NAME.CREATED"
    }

    var videoPlayerView: DanmakuVideoPlayer? = null
    var playerState = 0
        set(value) {
            field = value
            updatePlayerState()
        }

    private val playingNotification by lazy { PlayingNotification(this) }

    val mediaSession by lazy {
        MediaSessionCompat(this, "BilimiaoPlayer").apply {
            setCallback(mediaSessionCallback)
        }
    }
//    var mediaSession: MediaSession? = null
    private val info: PlayingInfo = PlayingInfo()
    private var showNotification = true // 是否显示通知栏控制器

    override fun onCreate() {
        super.onCreate()
        selfInstance = this


//        mediaSession?.setCallback(mediaSessionCallback)

        sendBroadcast(Intent(ACTION_CREATED))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action != null) {
            when (intent.action) {
                PlayingNotification.ACTION_CMD_TOGGLE_PAUSE -> {
                    clickTogglePause()
                }
                PlayingNotification.ACTION_CMD_CLOSE -> {
                    closePlayer()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun clickTogglePause() {
        if (playerState == GSYVideoPlayer.CURRENT_STATE_PLAYING) {
            videoPlayerView?.onVideoPause()
        } else {
            videoPlayerView?.onVideoResume()
        }
    }

    private fun closePlayer() {
        videoPlayerView?.release()
        videoPlayerView?.closeVideo()
        clearPlayingInfo()
    }

    override fun onDestroy() {
        super.onDestroy()
        selfInstance = null
    }

    fun setPlayingInfo(
        title: String,
        author: String,
        cover: String,
        duration: Long,
    ) {
        info.title = title
        info.author = author
        info.cover = cover
        info.duration = duration
        showNotification = getShowNotification()
        if (showNotification) {
            mediaSession?.isActive = true
            setupMediaSession()
            playingNotification.setPlayingInfo(info)
        }
    }

    fun clearPlayingInfo() {
        info.title = null
        info.author = null
        info.cover = null
        info.duration = 0L
        mediaSession?.isActive = false
        playingNotification.cancel()
    }


    fun getPlayingInfo(): PlayingInfo {
        return info
    }

    fun isPlaying(): Boolean {
        return playerState == GSYVideoPlayer.CURRENT_STATE_PLAYING ||
                playerState == GSYVideoPlayer.CURRENT_STATE_PLAYING_BUFFERING_START
    }

    /**
     * 是否显示通知栏控制器
     */
    private fun getShowNotification(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return prefs.getBoolean(VideoSettingFragment.PLAYER_PLAYING_NOTIFICATION, true)!!
    }

    private fun updatePlayerState() {
        if (!showNotification) {
            return
        }
        if (!isPlaying()) {
            // 只更新暂停状态，播放状态走播放回调
            val stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlayingNotification.MEDIA_SESSION_ACTIONS)
                .setState(
                    PlaybackStateCompat.STATE_PAUSED,
                    videoPlayerView?.currentPositionWhenPlaying ?: 0L,
                    1.0f
                )
            mediaSession.setPlaybackState(stateBuilder.build())
        }
        playingNotification.updateForPlaying()
    }

    fun setProgress(max: Long, progress: Long) {
        if (showNotification && info.title != null) {
            playingNotification.updateWithProgress(max, progress)
            val stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlayingNotification.MEDIA_SESSION_ACTIONS)
                .setState(
                    if (isPlaying()) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                    progress,
                    1.0f
                )
//        setCustomAction(stateBuilder)
            mediaSession.setPlaybackState(stateBuilder.build())
        }
    }

    private fun setupMediaSession() {
        val metaData = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, info.author ?: "bilimiao")
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, info.title ?: "bilimiao正在播放")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, info.duration)
//            .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, (getPosition() + 1).toLong())

        mediaSession?.setMetadata(metaData.build())
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPause() {
            super.onPause()
            videoPlayerView?.onVideoPause()
        }

        override fun onPlay() {
            super.onPlay()
            videoPlayerView?.onVideoResume()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            videoPlayerView?.seekTo(pos)
        }
    }

    data class PlayingInfo(
        var title: String? = null,
        var author: String? = null,
        var cover: String? = null,
        var duration: Long = 0L
    )
}