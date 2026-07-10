package com.a10miaomiao.bilimiao.comm.delegate.player

import cn.a10miaomiao.bilimiao.danmaku.parser.BaseDanmakuParser
import cn.a10miaomiao.bilimiao.danmaku.parser.BiliDanmakuParser
import cn.a10miaomiao.bilimiao.danmaku.parser.IDataSource
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceIds
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import com.a10miaomiao.bilimiao.comm.utils.CompressionTools
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.features.PlaybackSpeed
import org.openani.mediamp.mpv.MPVHandle
import org.openani.mediamp.mpv.MpvMediampPlayer
import org.openani.mediamp.source.UriMediaData
import org.openani.mediamp.playUri
import java.io.File

class DesktopPlayerDelegate(
    private val playerStore: PlayerStore,
    private val playListStore: PlayListStore,
) : BasePlayerDelegate {

    // 播放器实例
    private var _mediampPlayer: MediampPlayer? = null
    val mediampPlayer: MediampPlayer? get() = _mediampPlayer

    // 播放器显示/隐藏回调（由 Main.kt 设置）
    var onShowPlayerChanged: ((Boolean) -> Unit)? = null

    // 播放参数
    private var quality = 64 // 默认 720P
    private var fnval = 4048 // DASH 格式

    // 播放状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _loadingMessage = MutableStateFlow("")
    val loadingMessage: StateFlow<String> = _loadingMessage

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _isPlaying = MutableStateFlow(false)
    val isPlayingState: StateFlow<Boolean> = _isPlaying

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted

    private val _currentSource = MutableStateFlow<BasePlayerSource?>(null)
    val currentSource: StateFlow<BasePlayerSource?> = _currentSource

    private val _danmakuParser = MutableStateFlow<BaseDanmakuParser?>(null)
    val danmakuParser: StateFlow<BaseDanmakuParser?> = _danmakuParser

    private val _danmakuVisible = MutableStateFlow(true)
    val danmakuVisible: StateFlow<Boolean> = _danmakuVisible

    private val _volume = MutableStateFlow(100)
    val volume: StateFlow<Int> = _volume

    private val _playerSourceInfo = MutableStateFlow<PlayerSourceInfo?>(null)
    val playerSourceInfo: StateFlow<PlayerSourceInfo?> = _playerSourceInfo

    private val _currentQuality = MutableStateFlow(64)
    val currentQuality: StateFlow<Int> = _currentQuality

    // 分段播放状态
    private var segmentUrls = listOf<String>()
    private var segmentDurations = listOf<Long>()
    private var currentSegmentIndex = 0
    private var segmentOffsetMs = 0L // 当前段之前的累计时长
    private var segmentHeaders: Map<String, String> = emptyMap()

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    fun createPlayer(): MediampPlayer {
        initMpvNativeLibraries()
        val player = MpvMediampPlayer(Unit, Dispatchers.Main)
        _mediampPlayer = player
        return player
    }

    private fun initMpvNativeLibraries() {
        // Native libraries are extracted automatically by mediamp-native-loader
        // from the classpath (mediamp-mpv-runtime-windows-x64 JAR)
        MPVHandle.useDefaultRuntimeLibraryDirectory()
    }

    override fun openPlayer(source: BasePlayerSource) {
        _currentSource.value = source
        playerStore.setPlayerSource(source)
        onShowPlayerChanged?.invoke(true)
        loadAndPlay(source)
    }

    private fun loadAndPlay(source: BasePlayerSource) {
        val player = _mediampPlayer ?: return
        // 先停止之前的播放
        progressJob?.cancel()
        player.stopPlayback()
        coroutineScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isCompleted.value = false
            segmentUrls = emptyList()
            segmentDurations = emptyList()
            currentSegmentIndex = 0
            segmentOffsetMs = 0L
            try {
                // 获取弹幕数据
                _loadingMessage.value = "正在加载弹幕..."
                launch(Dispatchers.IO) {
                    try {
                        val res = BiliApiService.playerAPI.getDanmakuList(source.id).awaitCall()
                        val body = res.body
                        if (body != null) {
                            val xmlBytes = CompressionTools.decompressXML(body.bytes())
                            val dataSource = object : IDataSource<ByteArray> {
                                override fun data() = xmlBytes
                                override fun release() {}
                            }
                            val parser = BiliDanmakuParser()
                            parser.load(dataSource)
                            _danmakuParser.value = parser
                        }
                    } catch (e: Exception) {
                        _danmakuParser.value = null
                    }
                }

                // 获取播放地址
                _loadingMessage.value = "正在获取播放地址..."
                var sourceInfo = source.getPlayerUrl(quality, fnval)
                _playerSourceInfo.value = sourceInfo
                _currentQuality.value = sourceInfo.quality
                _duration.value = sourceInfo.duration

                // 解析 URL 格式
                val resolved = resolvePlaybackUrl(sourceInfo.url)
                _loadingMessage.value = "正在启动播放..."

                // 构建 HTTP 请求头，通过 UriMediaData 传递给 mediamp
                val headers = sourceInfo.header
                println("[BiliMiao] HTTP headers: $headers")

                when (resolved.format) {
                    PlaybackFormat.MERGING -> {
                        // 音视频分离：通过 mpv audio-files 选项在 loadfile 时一并加载外部音频
                        // audio-files 在 setMediaData 设置 headers 后、resume 调用 loadfile 前生效
                        player.setMediaData(UriMediaData(resolved.videoUrl, headers))
                        if (player is MpvMediampPlayer && resolved.audioUrl != null) {
                            (player.impl as MPVHandle).setPropertyString("audio-files", resolved.audioUrl)
                            println("[BiliMiao] Set external audio: ${resolved.audioUrl}")
                        }
                    }
                    PlaybackFormat.SEGMENTED -> {
                        // 分段视频：播放第一段
                        segmentUrls = resolved.segmentUrls
                        segmentDurations = resolved.segmentDurations
                        segmentHeaders = headers
                        currentSegmentIndex = 0
                        segmentOffsetMs = 0L
                        if (segmentUrls.isNotEmpty()) {
                            player.setMediaData(UriMediaData(segmentUrls.first(), headers))
                        }
                    }
                    PlaybackFormat.SINGLE -> {
                        player.setMediaData(UriMediaData(resolved.videoUrl, headers))
                    }
                    PlaybackFormat.TEMP_MPD -> {
                        // [dash-mpd] 格式：将 MPD XML 写入临时文件播放
                        val mpdFile = createTempMpdFile(resolved.mpdContent!!)
                        if (mpdFile != null) {
                            player.playUri(mpdFile.absolutePath)
                        } else {
                            player.setMediaData(UriMediaData(resolved.videoUrl, headers))
                        }
                    }
                }

                // playUri 只设置媒体数据（状态变为 READY），需要调用 resume 开始播放
                println("[BiliMiao] About to call resume(), state=${player.getCurrentPlaybackState()}")
                player.resume()
                println("[BiliMiao] resume() called, state=${player.getCurrentPlaybackState()}")
                _isPlaying.value = true

                // 播放历史恢复
                if (sourceInfo.lastPlayCid == source.id
                    && sourceInfo.lastPlayTime > 0
                    && sourceInfo.lastPlayTime < sourceInfo.duration - 10000
                ) {
                    delay(300) // 等待播放器加载
                    seekTo(sourceInfo.lastPlayTime)
                    GlobalToaster.show("自动恢复: ${formatTime(sourceInfo.lastPlayTime)}")
                }

                // 开始进度跟踪
                startProgressTracking(source)
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = e.message ?: "播放失败"
            } finally {
                _isLoading.value = false
                _loadingMessage.value = ""
            }
        }
    }

    private data class ResolvedPlayback(
        val videoUrl: String,
        val audioUrl: String? = null,
        val format: PlaybackFormat = PlaybackFormat.SINGLE,
        val segmentUrls: List<String> = emptyList(),
        val segmentDurations: List<Long> = emptyList(),
        val mpdContent: String? = null,
    )

    private enum class PlaybackFormat { SINGLE, MERGING, SEGMENTED, TEMP_MPD }

    private fun resolvePlaybackUrl(url: String): ResolvedPlayback {
        val trimmed = url.trim()
        return when {
            trimmed.startsWith("[merging]") -> {
                val urls = trimmed.removePrefix("[merging]")
                    .lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                val videoUrl = urls.firstOrNull() ?: throw Exception("未找到视频流地址")
                val audioUrl = urls.getOrNull(1)
                ResolvedPlayback(
                    videoUrl = videoUrl,
                    audioUrl = audioUrl,
                    format = PlaybackFormat.MERGING,
                )
            }
            trimmed.startsWith("[concatenating]") -> {
                val urls = trimmed.removePrefix("[concatenating]")
                    .lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                if (urls.isEmpty()) throw Exception("未找到视频分段地址")
                ResolvedPlayback(
                    videoUrl = urls.first(),
                    format = PlaybackFormat.SEGMENTED,
                    segmentUrls = urls,
                    segmentDurations = List(urls.size) { 0L },
                )
            }
            trimmed.startsWith("[dash-mpd]") -> {
                // [dash-mpd] 格式: [dash-mpd]\n<videoUrl>\n<mpdXml>
                val content = trimmed.removePrefix("[dash-mpd]").trim()
                val lines = content.lines().filter { it.isNotBlank() }
                val videoUrl = lines.firstOrNull() ?: throw Exception("未找到视频流地址")
                val mpdXml = lines.drop(1).joinToString("\n").trim()
                if (mpdXml.isNotBlank()) {
                    ResolvedPlayback(
                        videoUrl = videoUrl,
                        format = PlaybackFormat.TEMP_MPD,
                        mpdContent = mpdXml,
                    )
                } else {
                    ResolvedPlayback(videoUrl = videoUrl)
                }
            }
            else -> ResolvedPlayback(videoUrl = trimmed.replace("\n", "").replace("\r", "").replace(" ", ""))
        }
    }

    /**
     * 将 MPD XML 内容写入临时文件
     */
    private fun createTempMpdFile(mpdXml: String): File? {
        return try {
            val tempFile = File.createTempFile("bilimiao_dash_", ".mpd")
            tempFile.writeText(mpdXml)
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun startProgressTracking(source: BasePlayerSource) {
        progressJob?.cancel()
        progressJob = coroutineScope.launch {
            while (isActive) {
                delay(200) // 200ms 更新一次，减少弹幕 wall clock 推进累积误差
                _mediampPlayer?.let { player ->
                    val pos = player.currentPositionMillis.value
                    _currentPosition.value = segmentOffsetMs + pos

                    // 检测播放完成
                    if (_duration.value > 0 && _currentPosition.value >= _duration.value - 1000) {
                        if (segmentUrls.isNotEmpty() && currentSegmentIndex < segmentUrls.size - 1) {
                            loadNextSegment(player)
                        } else if (!_isCompleted.value) {
                            _isCompleted.value = true
                            _isPlaying.value = false
                            return@let
                        }
                    }

                    // 检测分段结束
                    if (segmentUrls.isNotEmpty()
                        && currentSegmentIndex < segmentUrls.size - 1
                        && !_isCompleted.value
                    ) {
                        val segmentDuration = segmentDurations.getOrNull(currentSegmentIndex) ?: 0L
                        if (segmentDuration > 0 && pos >= segmentDuration - 500) {
                            loadNextSegment(player)
                        }
                    }
                }

                // 每5秒上报历史记录
                if (_currentPosition.value % 5000 < 1000) {
                    source.historyReport(_currentPosition.value / 1000)
                }
            }
        }
    }

    private suspend fun loadNextSegment(player: MediampPlayer) {
        currentSegmentIndex++
        if (currentSegmentIndex in segmentUrls.indices) {
            val actualDuration = player.currentPositionMillis.value
            if (segmentDurations[currentSegmentIndex - 1] == 0L) {
                segmentDurations = segmentDurations.toMutableList().also {
                    it[currentSegmentIndex - 1] = actualDuration
                }
            }
            segmentOffsetMs += actualDuration
            player.setMediaData(UriMediaData(segmentUrls[currentSegmentIndex], segmentHeaders))
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
            _isCompleted.value = false
        }
    }

    fun seekTo(positionMs: Long) {
        _mediampPlayer?.let { player ->
            if (segmentUrls.isNotEmpty()) {
                var accumulated = 0L
                for (i in segmentUrls.indices) {
                    val segDur = segmentDurations.getOrNull(i)?.takeIf { it > 0 } ?: _duration.value / segmentUrls.size
                    if (positionMs < accumulated + segDur) {
                        if (i != currentSegmentIndex) {
                            currentSegmentIndex = i
                            segmentOffsetMs = accumulated
                            coroutineScope.launch { player.setMediaData(UriMediaData(segmentUrls[i], segmentHeaders)) }
                        }
                        player.seekTo(positionMs - accumulated)
                        _currentPosition.value = positionMs
                        return
                    }
                    accumulated += segDur
                }
            }
            player.seekTo(positionMs)
            _currentPosition.value = positionMs
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _mediampPlayer?.let { player ->
            player.features[PlaybackSpeed]?.set(speed)
        }
    }

    fun changeQuality(newQuality: Int) {
        val source = _currentSource.value ?: return
        quality = newQuality
        val savedPosition = _currentPosition.value
        loadAndPlay(source)
        coroutineScope.launch {
            delay(500)
            seekTo(savedPosition)
        }
    }

    fun toggleDanmaku() {
        _danmakuVisible.value = !_danmakuVisible.value
    }

    fun setVolume(newVolume: Int) {
        val clamped = newVolume.coerceIn(0, 100)
        _volume.value = clamped
        try {
            val player = _mediampPlayer
            if (player is MpvMediampPlayer) {
                (player.impl as MPVHandle).setPropertyInt("volume", clamped)
            }
        } catch (_: Exception) {}
    }

    fun replay() {
        _isCompleted.value = false
        seekTo(0)
        resume()
    }

    fun playNext() {
        val source = _currentSource.value ?: return
        val next = source.next() ?: return
        openPlayer(next)
    }

    fun retry() {
        val source = _currentSource.value ?: return
        loadAndPlay(source)
    }

    override fun closePlayer() {
        progressJob?.cancel()
        _mediampPlayer?.let { player ->
            player.stopPlayback()
        }
        _currentSource.value = null
        _danmakuParser.value?.release()
        _danmakuParser.value = null
        _currentPosition.value = 0L
        _duration.value = 0L
        _isPlaying.value = false
        _isCompleted.value = false
        _errorMessage.value = null
        _playerSourceInfo.value = null
        segmentUrls = emptyList()
        segmentDurations = emptyList()
        currentSegmentIndex = 0
        segmentOffsetMs = 0L
        playerStore.clearPlayerInfo()
        onShowPlayerChanged?.invoke(false)
    }

    override fun currentPosition(): Long = _currentPosition.value

    override fun isPlaying(): Boolean = _isPlaying.value

    override fun isPause(): Boolean = !_isPlaying.value

    override fun isOpened(): Boolean = _currentSource.value != null

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
        // TODO: 实现弹幕发送后的本地显示
    }

    override fun setProxy(proxyServer: ProxyServerInfo, uposHost: String) {
        // TODO: 设置代理
    }

    companion object {
        private fun formatTime(ms: Long): String {
            val seconds = ms / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            return if (hours > 0) {
                "%d:%02d:%02d".format(hours, minutes % 60, seconds % 60)
            } else {
                "%02d:%02d".format(minutes, seconds % 60)
            }
        }
    }
}
