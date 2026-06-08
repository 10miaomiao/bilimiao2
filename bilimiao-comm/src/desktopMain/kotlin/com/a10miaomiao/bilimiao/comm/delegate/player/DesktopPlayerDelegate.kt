package com.a10miaomiao.bilimiao.comm.delegate.player

import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceIds
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.features.PlaybackSpeed
import org.openani.mediamp.playUri
import org.openani.mediamp.vlc.VlcMediampPlayer
import org.openani.mediamp.vlc.loader.MediampVlcLoader

class DesktopPlayerDelegate(
    private val playerStore: PlayerStore,
    private val playListStore: PlayListStore,
) : BasePlayerDelegate {

    // 播放器实例
    private var _mediampPlayer: MediampPlayer? = null
    val mediampPlayer: MediampPlayer? get() = _mediampPlayer

    // 播放器显示/隐藏回调（由 Main.kt 设置）
    var onShowPlayerChanged: ((Boolean) -> Unit)? = null

    // 播放状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _isPlaying = MutableStateFlow(false)
    val isPlayingState: StateFlow<Boolean> = _isPlaying

    private val _currentSource = MutableStateFlow<BasePlayerSource?>(null)
    val currentSource: StateFlow<BasePlayerSource?> = _currentSource

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    fun createPlayer(): MediampPlayer {
        // 启用测试发现目录（开发环境加载 VLC）
        val appResourcesDir = java.io.File(System.getProperty("user.dir"), "desktop-app/appResources/windows-x64")
        if (appResourcesDir.exists()) {
            MediampVlcLoader.enableTestDiscovery(appResourcesDir)
        }

        // 准备 VLC 库
        VlcMediampPlayer.prepareLibraries()

        // 创建 VLC 播放器
        val player = VlcMediampPlayer(Dispatchers.Main)
        _mediampPlayer = player
        return player
    }

    override fun openPlayer(source: BasePlayerSource) {
        _currentSource.value = source
        playerStore.setPlayerSource(source)
        onShowPlayerChanged?.invoke(true)
        loadAndPlay(source)
    }

    private fun loadAndPlay(source: BasePlayerSource) {
        val player = _mediampPlayer ?: return
        coroutineScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // 获取播放地址，fnval 使用默认值 0
                val sourceInfo = source.getPlayerUrl(
                    source.defaultPlayerSource.quality,
                    0 // fnval 默认值
                )
                // 解析 URL 格式
                val url = resolvePlaybackUrl(sourceInfo.url)
                // 使用 mediamp 播放
                player.playUri(url)
                // 更新时长
                _duration.value = sourceInfo.duration
                // 更新播放状态
                _isPlaying.value = true
                // 开始进度跟踪
                startProgressTracking(source)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "播放失败"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun resolvePlaybackUrl(url: String): String {
        // 清理 URL：去除换行符、空格等空白字符
        val cleanUrl = url.trim().replace("\n", "").replace("\r", "").replace(" ", "")
        return when {
            cleanUrl.startsWith("[merging]") -> {
                // DASH 音视频分离：直接播放视频流
                cleanUrl.removePrefix("[merging]")
            }
            cleanUrl.startsWith("[concatenating]") -> {
                // FLV 分段拼接：播放第一段
                cleanUrl.removePrefix("[concatenating]").split(";").first()
            }
            cleanUrl.startsWith("[dash-mpd]") -> {
                // DASH MPD：直接播放
                cleanUrl.removePrefix("[dash-mpd]")
            }
            else -> cleanUrl
        }
    }

    private fun startProgressTracking(source: BasePlayerSource) {
        progressJob?.cancel()
        progressJob = coroutineScope.launch {
            while (isActive) {
                delay(1000) // 每秒更新位置
                // 从播放器获取当前位置
                _mediampPlayer?.let { player ->
                    _currentPosition.value = player.currentPositionMillis.value
                }

                // 每5秒上报历史记录
                if (_currentPosition.value % 5000 < 1000) {
                    source.historyReport(_currentPosition.value)
                }
            }
        }
    }

    fun pause() {
        _mediampPlayer?.let { player ->
            player.pause()
            _isPlaying.value = false
        }
    }

    fun resume() {
        _mediampPlayer?.let { player ->
            player.resume()
            _isPlaying.value = true
        }
    }

    fun seekTo(positionMs: Long) {
        _mediampPlayer?.let { player ->
            player.seekTo(positionMs)
            _currentPosition.value = positionMs
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _mediampPlayer?.let { player ->
            player.features[PlaybackSpeed]?.set(speed)
        }
    }

    override fun closePlayer() {
        progressJob?.cancel()
        _mediampPlayer?.let { player ->
            player.stopPlayback()
        }
        _currentSource.value = null
        _currentPosition.value = 0L
        _duration.value = 0L
        _isPlaying.value = false
        _errorMessage.value = null
        onShowPlayerChanged?.invoke(false)
    }

    override fun currentPosition(): Long {
        return _currentPosition.value
    }

    override fun isPlaying(): Boolean {
        return _isPlaying.value
    }

    override fun isPause(): Boolean {
        return !_isPlaying.value
    }

    override fun isOpened(): Boolean {
        return _currentSource.value != null
    }

    override fun getSourceIds(): PlayerSourceIds {
        return _currentSource.value?.getSourceIds() ?: PlayerSourceIds()
    }

    // BaseDelegate 生命周期方法
    override fun onCreate() {}
    override fun onResume() {}
    override fun onPause() {}
    override fun onStart() {}
    override fun onStop() {}
    override fun onDestroy() {
        coroutineScope.cancel()
    }

    override fun onBackPressed(): Boolean {
        if (isOpened()) {
            closePlayer()
            return true
        }
        return false
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {}
    override fun setWindowInsets(left: Int, top: Int, right: Int, bottom: Int) {}
    override fun onConfigurationChanged(orientation: Int) {}

    override fun sendDanmaku(
        type: Int,
        danmakuText: String,
        danmakuTextSize: Float,
        danmakuTextColor: Int,
        danmakuPosition: Long
    ) {
        // 暂不支持弹幕
    }

    override fun setProxy(proxyServer: ProxyServerInfo, uposHost: String) {
        // TODO: 设置代理
    }
}
