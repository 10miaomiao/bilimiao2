package com.a10miaomiao.bilimiao.comm.delegate.player

import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlaybackState
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceIds
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceState
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import org.openani.mediamp.MediampPlayer

/**
 * 统一播放器代理接口（KMP）
 *
 * 安卓端和桌面端共用此接口，统一基于 [MediampPlayer] 抽象层。
 * 平台差异（player 创建、音视频分离处理、音量控制等）由 [createMediampPlayer] 等
 * expect/actual 函数处理。
 *
 * 替代原安卓端的 PlayerDelegate2 (基于 GSYVideoPlayer) 和桌面端的 DesktopPlayerDelegate，
 * 合并为单一接口，由 [PlayerDelegateImpl] (commonMain) 提供默认实现。
 */
interface BasePlayerDelegate : com.a10miaomiao.bilimiao.comm.delegate.BaseDelegate {
    /**
     * 当前播放器实例（可能为 null，未创建时）
     */
    val mediampPlayer: MediampPlayer?

    /**
     * 播放器显示/隐藏回调（由入口点设置，用于触发 UI 显示/隐藏播放器容器）
     */
    var onShowPlayerChanged: ((Boolean) -> Unit)?

    /**
     * 播放状态（低频稳定状态，见 [PlaybackState]）
     */
    val playbackState: kotlinx.coroutines.flow.StateFlow<PlaybackState>

    /**
     * 播放源状态（当前播放内容，见 [PlayerSourceState]）
     */
    val sourceState: kotlinx.coroutines.flow.StateFlow<PlayerSourceState>

    /**
     * 当前播放位置（毫秒），高频更新（约 200ms 一次），独立 StateFlow 避免触发无关重组
     */
    val currentPosition: kotlinx.coroutines.flow.StateFlow<Long>

    /**
     * 创建平台特定的 MediampPlayer 实例
     */
    fun createPlayer(): MediampPlayer

    /**
     * 打开播放源，开始加载和播放
     */
    fun openPlayer(source: BasePlayerSource)

    /**
     * 关闭播放器，释放资源
     */
    fun closePlayer()

    /**
     * 暂停
     */
    fun pause()

    /**
     * 恢复播放
     */
    fun resume()

    /**
     * 跳转到指定位置（毫秒）
     */
    fun seekTo(positionMs: Long)

    /**
     * 设置播放倍速
     */
    fun setPlaybackSpeed(speed: Float)

    /**
     * 切换清晰度
     */
    fun changeQuality(newQuality: Int)

    /**
     * 切换字幕（null 表示关闭字幕）
     */
    fun setSubtitle(subtitle: com.a10miaomiao.bilimiao.comm.delegate.player.entity.SubtitleSourceInfo?)

    /**
     * 切换弹幕显示
     */
    fun toggleDanmaku()

    /**
     * 设置音量 (0-100)
     */
    fun setVolume(newVolume: Int)

    /**
     * 重播当前视频
     */
    fun replay()

    /**
     * 播放下一个
     */
    fun playNext()

    /**
     * 重试当前播放
     */
    fun retry()

    // === 兼容旧接口方法 ===
    fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean)
    fun isOpened(): Boolean
    fun isPlaying(): Boolean
    fun isPause(): Boolean
    fun setWindowInsets(left: Int, top: Int, right: Int, bottom: Int)
    fun onConfigurationChanged(orientation: Int)
    fun getSourceIds(): PlayerSourceIds
    fun currentPosition(): Long
    fun sendDanmaku(type: Int, danmakuText: String, danmakuTextSize: Float, danmakuTextColor: Int, danmakuPosition: Long)
    fun setProxy(proxyServer: ProxyServerInfo, uposHost: String)
}
