package com.a10miaomiao.bilimiao.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.service.notification.PlayingNotification
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PlayerService : Service() {

    companion object {
        val TAG: String = PlayerService::class.java.simpleName
        var selfInstance: PlayerService? = null
            private set

        const val BILIMIAO_PLAYER_PACKAGE_NAME = "com.a10miaomiao.bilimiao.player"
        const val ACTION_CREATED = "$BILIMIAO_PLAYER_PACKAGE_NAME.CREATED"
        const val ACTION_DESTROY = "$BILIMIAO_PLAYER_PACKAGE_NAME.DESTROY"
    }

    var videoPlayerView: DanmakuVideoPlayer? = null
    var playerState = 0
        set(value) {
            field = value
            updatePlayerState()
        }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val playingNotification by lazy { PlayingNotification(this, serviceScope) }

    private var mediaSession: MediaSessionCompat? = null
    private var showNotification = true // 是否显示通知栏控制器

    override fun onCreate() {
        super.onCreate()
        selfInstance = this
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
        selfInstance = null
        sendBroadcast(Intent(ACTION_DESTROY))
        playingNotification.cancel()
        super.onDestroy()
    }

    fun setPlayingInfo(info: PlayingInfo) {
        setupMediaSession(info)
        mediaSession?.isActive = true
        serviceScope.launch {
            showNotification = SettingPreferences.mapData(this@PlayerService) {
                it[PlayerNotification] ?: true
            }
            if (showNotification) {
                playingNotification.setPlayingInfo(mediaSession, info)
            }
        }
    }

    fun clearPlayingInfo() {
        mediaSession?.isActive = false
        mediaSession = null
        playingNotification.cancel()
    }

    fun isPlaying(): Boolean {
        return playerState == GSYVideoPlayer.CURRENT_STATE_PLAYING ||
                playerState == GSYVideoPlayer.CURRENT_STATE_PLAYING_BUFFERING_START
    }

    private fun updatePlayerState() {
        try {
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
                mediaSession?.setPlaybackState(stateBuilder.build())
            }
            playingNotification.updateForPlaying(mediaSession)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun setProgress(max: Long, progress: Long) {
        if (showNotification && mediaSession != null) {
            playingNotification.updateWithProgress(mediaSession, max, progress)
            val stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlayingNotification.MEDIA_SESSION_ACTIONS)
                .setState(
                    if (isPlaying()) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                    progress,
                    1.0f
                )
//        setCustomAction(stateBuilder)
            mediaSession?.setPlaybackState(stateBuilder.build())
        }
    }

    private fun setupMediaSession(info: PlayingInfo) {
        val mediaSession = MediaSessionCompat(this, "BilimiaoPlayer")
        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(PlayingNotification.MEDIA_SESSION_ACTIONS)
            .setState(
                if (isPlaying()) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                0,
                1.0f
            )
        mediaSession.setPlaybackState(stateBuilder.build())
        mediaSession.setCallback(mediaSessionCallback)
        val metaData = getMediaMetadata(info)
        mediaSession.setMetadata(metaData.build())
        this.mediaSession = mediaSession
    }

    fun getMediaMetadata(info: PlayingInfo): MediaMetadataCompat.Builder {
        val metaData = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, info.author ?: "bilimiao")
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, info.author ?: "bilimiao")
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, info.title ?: "bilimiao正在播放")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, info.duration)
        //            .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, (getPosition() + 1).toLong())
        return metaData
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

        override fun onStop() {
            super.onStop()
            videoPlayerView?.closeVideo()
        }
    }

    data class PlayingInfo(
        var title: String? = null,
        var author: String? = null,
        var cover: String? = null,
        var duration: Long = 0L
    )
}