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
import com.a10miaomiao.bilimiao.comm.datastore.appDataStore
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.MediaSessionBridge
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PlaybackService : MediaSessionService(), MediaSession.Callback, MediaSessionBridge.Connector {

    companion object {
        var instance: PlaybackService? = null
            private set
    }

    private var exoPlayer: ExoPlayer? = null

    /**
     * 服务自建的占位播放器
     *
     * 在真实播放器（PlayerDelegateImpl 的 ExoPlayer）接管之前交给 MediaSession，
     * 避免 MediaSession 必须持有可空播放器；真实播放器接管后立即释放。
     */
    private var defaultPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var showNotification = true
    private var playerDelegate: BasePlayerDelegate? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeSessionAndPlayer()
        // 与播放器建立桥接：播放器已存在则立即接管，否则等其创建时回调
        MediaSessionBridge.registerConnector(this)
        serviceScope.launch {
            initPlayerSetting()
        }
    }

    private suspend fun initPlayerSetting() {
        var isInitial = true
        appDataStore.data.map {
            it[SettingPreferences.PlayerNotification] ?: true
        }.collect {
            showNotification = it
            if (isInitial) {
                isInitial = false
            } else {
                if (showNotification) {
                    exoPlayer?.let {
                        mediaSession?.player = MyForwardingPlayer(it)
                    }
                    // MediaSession 已换回真实播放器，释放关闭期间使用的占位播放器
                    defaultPlayer?.takeIf { it !== exoPlayer }?.release()
                    defaultPlayer = null
                } else {
                    // 关闭通知栏控制器：MediaSession 换用不承载播放的占位播放器
                    val blankPlayer = defaultPlayer ?: defaultExoPlayer().also { defaultPlayer = it }
                    mediaSession?.player = blankPlayer
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
        defaultPlayer = player
        exoPlayer = player
    }

    /**
     * 接管播放器（[MediaSessionBridge.Connector] 回调）
     *
     * 播放器实例由 PlayerDelegateImpl 创建并持有，服务只把它交给 MediaSession，
     * 由 media3 在播放时自动生成通知栏播放器控制器。重复接管同一实例是幂等的。
     */
    override fun attachPlayer(player: ExoPlayer) {
        if (exoPlayer === player) {
            if (showNotification) {
                mediaSession?.player = MyForwardingPlayer(player)
            }
            return
        }
        exoPlayer = player
        if (showNotification) {
            // 先让 MediaSession 换用真实播放器，再释放服务自建的占位播放器；
            // 通知栏控制器关闭期间占位播放器仍被 MediaSession 持有，留到重新开启时释放
            mediaSession?.player = MyForwardingPlayer(player)
            defaultPlayer?.takeIf { it !== player }?.release()
            defaultPlayer = null
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
        MediaSessionBridge.unregisterConnector(this)
        // 先释放 MediaSession（解除对播放器的引用），再释放服务自建的占位播放器；
        // 外部播放器由 PlayerDelegateImpl 持有，服务不负责释放
        mediaSession?.release()
        mediaSession = null
        defaultPlayer?.release()
        defaultPlayer = null
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