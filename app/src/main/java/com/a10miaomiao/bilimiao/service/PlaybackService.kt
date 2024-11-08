package com.a10miaomiao.bilimiao.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.a10miaomiao.bilimiao.MainActivity
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences.dataStore
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PlaybackService : MediaSessionService(), MediaSession.Callback {

    companion object {
        var instance: PlaybackService? = null
            private set
    }

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var showNotification = true
    private var playerDelegate: BasePlayerDelegate? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeSessionAndPlayer()
        serviceScope.launch {
            initPlayerSetting()
        }
    }

    private suspend fun initPlayerSetting() = SettingPreferences.run {
        var isInitial = true
        dataStore.data.map {
            it[PlayerNotification] ?: true
        }.collect {
            showNotification = it
            if (isInitial) {
                isInitial = false
            } else {
                if (showNotification) {
                    exoPlayer?.let {
                        mediaSession?.player = it
                    }
                } else {
                    mediaSession?.player = defaultExoPlayer()
                }
            }
        }
    }

    private fun initializeSessionAndPlayer() {
        val player = defaultExoPlayer()
        val intent = Intent(this, MainActivity::class.java)
        val pIntent: PendingIntent = PendingIntent.getActivity(
            this,
            1,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(this)
            .setSessionActivity(pIntent)
            .build()
        exoPlayer = player
    }

    fun setPlayer(player: ExoPlayer) {
        exoPlayer?.release()
        exoPlayer = player
        if (showNotification) {
            mediaSession?.player = MyForwardingPlayer(player)
        }
    }

    fun setPlayerDelegate(delegate: BasePlayerDelegate) {
        playerDelegate = delegate
    }

    private fun defaultExoPlayer() = ExoPlayer.Builder(this).build()

    // The user dismissed the app from the recent tasks
    override fun onTaskRemoved(rootIntent: Intent?) {
//        val player = mediaSession?.player!!
//        if (!player.playWhenReady
//            || player.mediaItemCount == 0
//            || player.playbackState == Player.STATE_ENDED) {
//            // Stop the service if not playing, continue playing in the background
//            // otherwise.
//            stopSelf()
//        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    // Remember to release the player and media session in onDestroy
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        instance = null
        super.onDestroy()
        miaoLogger() debug "PlaybackService.onDestroy"
    }

    @OptIn(UnstableApi::class)
    inner class MyForwardingPlayer(player: Player) : ForwardingPlayer(player) {
        override fun stop() {
            super.stop()
            playerDelegate?.closePlayer()
        }
    }

}