package com.a10miaomiao.bilimiao.comm.delegate.player

import androidx.media3.exoplayer.ExoPlayer

/**
 * 通知栏播放器控制器（MediaSession）桥接
 *
 * 播放器实例（bilimiao-comm）与 [MediaSessionService][androidx.media3.session.MediaSessionService]
 * 实现（app 模块的 PlaybackService）分属不同 Gradle 模块，且两者的创建时序不确定：
 * 播放器随 MainActivity 创建，服务随 MediaController 绑定启动。
 *
 * 因此这里提供一个不依赖 app 模块的桥接点：
 * - 播放器创建后调用 [registerPlayer]
 * - 服务启动后注册 [Connector]，调用 [registerConnector]
 *
 * 双方任意一方后到位都会立即建立关联，由 Connector 接管 ExoPlayer，
 * 之后 media3 会在播放时自动生成通知栏播放器控制器。
 */
object MediaSessionBridge {

    interface Connector {
        /** 接管指定 ExoPlayer（MediaSession 将把它作为播放器，从而生成通知栏控制器） */
        fun attachPlayer(player: ExoPlayer)
    }

    @Volatile
    private var connector: Connector? = null

    @Volatile
    private var player: ExoPlayer? = null

    /**
     * 播放器创建后注册（由 [BiliExoPlayerMediampPlayer] 调用）
     */
    fun registerPlayer(newPlayer: ExoPlayer) {
        player = newPlayer
        connector?.attachPlayer(newPlayer)
    }

    /**
     * 播放器释放前注销（避免 MediaSession 继续持有已释放的播放器）
     */
    fun unregisterPlayer(releasedPlayer: ExoPlayer) {
        if (player === releasedPlayer) {
            player = null
        }
    }

    /**
     * 服务启动后注册（由 PlaybackService 调用）
     */
    fun registerConnector(newConnector: Connector) {
        connector = newConnector
        player?.let(newConnector::attachPlayer)
    }

    fun unregisterConnector(oldConnector: Connector) {
        if (connector === oldConnector) {
            connector = null
        }
    }
}
