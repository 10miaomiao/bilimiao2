package com.a10miaomiao.bilimiao.comm.delegate.player

import cn.a10miaomiao.bilimiao.danmaku.parser.BaseDanmakuParser
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.mapPreferences
import com.a10miaomiao.bilimiao.comm.entity.player.toVideoPlayerSource
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import com.a10miaomiao.bilimiao.comm.utils.CompressionTools
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceIds
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.SubtitleSourceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.features.PlaybackSpeed
import org.openani.mediamp.source.UriMediaData
import org.openani.mediamp.playUri

/**
 * 跨平台播放器代理实现
 *
 * 安卓端和桌面端共用的播放器核心逻辑，基于 [MediampPlayer] 抽象层。
 * 平台差异由 [createMediampPlayer]、[setMergingMediaData]、[setPlayerVolume] 等
 * expect/actual 函数处理。
 *
 * 此类替代原 [DesktopPlayerDelegate] (桌面) 和 [PlayerDelegate2] (安卓, 基于 GSY)，
 * 统一两端的播放、暂停、跳转、清晰度切换、弹幕加载、播放完成等行为。
 *
 * 参考实现：animeko (org.openani.mediamp) 的 VideoScaffold + MediampPlayer 模式。
 */
class PlayerDelegateImpl(
    private val playerStore: PlayerStore,
    private val playListStore: PlayListStore,
    isLockScreenOrientationPortraitProvider: () -> Boolean = { false },
) : BasePlayerDelegate {

    // 播放器实例
    private var _mediampPlayer: MediampPlayer? = null
    override val mediampPlayer: MediampPlayer? get() = _mediampPlayer

    override var onShowPlayerChanged: ((Boolean) -> Unit)? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    /** 全屏与屏幕方向控制器 */
    val fullscreenController = FullscreenController(
        scope = coroutineScope,
        isLockScreenOrientationPortraitProvider = isLockScreenOrientationPortraitProvider,
    )

    // 播放参数
    private var quality = 64 // 默认 720P
    private var fnval = 4048 // DASH 格式

    // 播放状态
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading

    private val _loadingMessage = MutableStateFlow("")
    override val loadingMessage: StateFlow<String> = _loadingMessage

    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage

    private val _currentPosition = MutableStateFlow(0L)
    override val currentPosition: StateFlow<Long> = _currentPosition

    private val _playbackSpeed = MutableStateFlow(1.0f)
    override val playbackSpeed: StateFlow<Float> = _playbackSpeed

    private val _duration = MutableStateFlow(0L)
    override val duration: StateFlow<Long> = _duration

    private val _isPlaying = MutableStateFlow(false)
    override val isPlayingState: StateFlow<Boolean> = _isPlaying

    private val _isCompleted = MutableStateFlow(false)
    override val isCompleted: StateFlow<Boolean> = _isCompleted

    private val _currentSource = MutableStateFlow<BasePlayerSource?>(null)
    override val currentSource: StateFlow<BasePlayerSource?> = _currentSource

    private val _danmakuParser = MutableStateFlow<BaseDanmakuParser?>(null)
    override val danmakuParser: StateFlow<BaseDanmakuParser?> = _danmakuParser

    private val _danmakuVisible = MutableStateFlow(true)
    override val danmakuVisible: StateFlow<Boolean> = _danmakuVisible

    private val _volume = MutableStateFlow(100)
    override val volume: StateFlow<Int> = _volume

    private val _playerSourceInfo = MutableStateFlow<PlayerSourceInfo?>(null)
    override val playerSourceInfo: StateFlow<PlayerSourceInfo?> = _playerSourceInfo

    private val _currentQuality = MutableStateFlow(64)
    override val currentQuality: StateFlow<Int> = _currentQuality

    // 字幕状态
    private val _subtitleList = MutableStateFlow<List<SubtitleSourceInfo>>(emptyList())
    override val subtitleList: StateFlow<List<SubtitleSourceInfo>> = _subtitleList

    private val _currentSubtitle = MutableStateFlow<SubtitleSourceInfo?>(null)
    override val currentSubtitle: StateFlow<SubtitleSourceInfo?> = _currentSubtitle

    // 分段播放状态
    private var segmentUrls = listOf<String>()
    private var segmentDurations = listOf<Long>()
    private var currentSegmentIndex = 0
    private var segmentOffsetMs = 0L // 当前段之前的累计时长
    private var segmentHeaders: Map<String, String> = emptyMap()


    override fun createPlayer(): MediampPlayer {
        val player = createMediampPlayer()
        _mediampPlayer = player
        return player
    }

    override fun openPlayer(source: BasePlayerSource) {
        _currentSource.value = source
        playerStore.setPlayerSource(source)
        onShowPlayerChanged?.invoke(true)
        loadAndPlay(source)
        // 检查是否默认全屏播放
        fullscreenController.checkIsPlayerDefaultFull()
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
            // 获取弹幕数据：本地下载文件优先读取 danmaku.xml，否则走网络接口
            _loadingMessage.value = "正在加载弹幕..."
            launch(Dispatchers.IO) {
                try {
                    val parser = loadDanmakuParser(source)
                    _danmakuParser.value = parser
                } catch (e: Exception) {
                    _danmakuParser.value = null
                }
            }

            // 获取 CC 字幕列表并设置默认字幕
            launch(Dispatchers.IO) {
                try {
                    val subtitles = source.getSubtitles()
                    _subtitleList.value = subtitles
                    _currentSubtitle.value = selectDefaultSubtitle(subtitles)
                } catch (e: Exception) {
                    _subtitleList.value = emptyList()
                    _currentSubtitle.value = null
                }
            }

                // 获取播放地址
                _loadingMessage.value = "正在获取播放地址..."
                val sourceInfo = source.getPlayerUrl(quality, fnval)
                _playerSourceInfo.value = sourceInfo
                fullscreenController.playerSourceInfo = sourceInfo
                _currentQuality.value = sourceInfo.quality
                _duration.value = sourceInfo.duration

                // 解析 URL 格式
                val resolved = resolvePlaybackUrl(sourceInfo.url)
                _loadingMessage.value = "正在启动播放..."

                val headers = sourceInfo.header

                when (resolved.format) {
                    PlaybackFormat.MERGING -> {
                        // 音视频分离：通过平台特定实现处理
                        // (安卓 ExoPlayer 用 MergingMediaSource，桌面 mpv 用 audio-files)
                        setMergingMediaData(player, resolved.videoUrl, resolved.audioUrl, headers)
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
                player.resume()
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
            trimmed.startsWith("[merging]") || trimmed.startsWith("[local-merging]") -> {
                // [merging]: 远程音视频分离; [local-merging]: 本地音视频分离
                val prefix = if (trimmed.startsWith("[local-merging]")) "[local-merging]" else "[merging]"
                val urls = trimmed.removePrefix(prefix)
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
     * 将 MPD XML 内容写入临时文件（平台特定实现）
     */
    private fun createTempMpdFile(mpdXml: String): java.io.File? {
        return try {
            val tempFile = java.io.File.createTempFile("bilimiao_dash_", ".mpd")
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
                            onAutoCompletion()
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

    /**
     * 播放完成后的自动播放逻辑（对齐原安卓版 PlayerController.onAutoCompletion）
     */
    private fun onAutoCompletion() {
        val source = _currentSource.value ?: return
        coroutineScope.launch {
            val (order, orderRandom) = SettingPreferences.mapPreferences {
                val order = it[SettingPreferences.PlayerOrder] ?: SettingConstants.PLAYER_ORDER_DEFAULT
                val orderRandom = it[SettingPreferences.PlayerOrderRandom] ?: false
                order to orderRandom
            }
            val isLoop = order and SettingConstants.PLAYER_ORDER_LOOP != 0
            val nextPlayerSourceInfo = source.next()
            if (nextPlayerSourceInfo is VideoPlayerSource
                && order and SettingConstants.PLAYER_ORDER_NEXT_P != 0
            ) {
                // 自动播放下一P
                openPlayer(nextPlayerSourceInfo)
                return@launch
            } else if (nextPlayerSourceInfo is BangumiPlayerSource
                && order and SettingConstants.PLAYER_ORDER_NEXT_EPISODE != 0
            ) {
                // 自动播放下一集
                openPlayer(nextPlayerSourceInfo)
                return@launch
            }
            if (order and SettingConstants.PLAYER_ORDER_NEXT_VIDEO != 0) {
                // 自动下一个视频
                val nextVideo = playerStore.nextVideo(orderRandom, isLoop)
                if (nextVideo != null) {
                    openPlayer(nextVideo.toVideoPlayerSource())
                    return@launch
                }
            }
            if (isLoop) {
                // 单个视频循环
                source.isLoop = true
                openPlayer(source)
            }
            // 否则保持 _isCompleted = true 状态，显示播放完成覆盖层
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

    override fun pause() {
        _mediampPlayer?.let { player ->
            player.pause()
            _isPlaying.value = false
        }
    }

    override fun resume() {
        _mediampPlayer?.let { player ->
            player.resume()
            _isPlaying.value = true
            _isCompleted.value = false
        }
    }

    override fun seekTo(positionMs: Long) {
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

    override fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        _mediampPlayer?.let { player ->
            player.features[PlaybackSpeed]?.set(speed)
        }
    }

    override fun changeQuality(newQuality: Int) {
        val source = _currentSource.value ?: return
        quality = newQuality
        val savedPosition = _currentPosition.value
        loadAndPlay(source)
        coroutineScope.launch {
            delay(500)
            seekTo(savedPosition)
        }
    }

    override fun toggleDanmaku() {
        _danmakuVisible.value = !_danmakuVisible.value
    }

    override fun setSubtitle(subtitle: SubtitleSourceInfo?) {
        _currentSubtitle.value = subtitle
        // TODO: 字幕渲染需接入播放管线。安卓 ExoPlayer 可通过 UriMediaData.extraFiles
        // 挂载外部字幕（参考 animeko SubtitleSwitcher），桌面 mpv（mediamp 0.1.14）
        // 尚未支持 extraFiles，后续随播放管线升级再接入实际字幕绘制。
    }

    /**
     * 根据设置选择默认字幕
     *
     * 对齐原安卓版 PlayerController.getDefaultSubtitle 的逻辑：
     * - PlayerSubtitleShow 关闭时不显示字幕
     * - 默认优先选择非 AI 字幕（ai_status == 0），开启 AI 字幕时允许 AI 字幕
     */
    private suspend fun selectDefaultSubtitle(
        list: List<SubtitleSourceInfo>,
    ): SubtitleSourceInfo? {
        if (list.isEmpty()) return null
        val (showSubtitle, showAiSubtitle) = SettingPreferences.mapPreferences {
            (it[SettingPreferences.PlayerSubtitleShow] ?: true) to
                (it[SettingPreferences.PlayerAiSubtitleShow] ?: false)
        }
        if (!showSubtitle) return null
        return list.find { showAiSubtitle || it.ai_status == 0 }
    }

    override fun setVolume(newVolume: Int) {
        val clamped = newVolume.coerceIn(0, 100)
        _volume.value = clamped
        _mediampPlayer?.let { player ->
            setPlayerVolume(player, clamped)
        }
    }

    override fun replay() {
        _isCompleted.value = false
        seekTo(0)
        resume()
    }

    override fun playNext() {
        val source = _currentSource.value ?: return
        val next = source.next() ?: return
        openPlayer(next)
    }

    override fun retry() {
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
        _subtitleList.value = emptyList()
        _currentSubtitle.value = null
        segmentUrls = emptyList()
        segmentDurations = emptyList()
        currentSegmentIndex = 0
        segmentOffsetMs = 0L
        playerStore.clearPlayerInfo()
        fullscreenController.smallScreen()
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

    /**
     * 加载弹幕解析器
     *
     * 优先检查 [LocalDanmakuSource]（本地下载文件的 danmaku.xml），
     * 若无则回退到 [BasePlayerSource.getDanmakuParser]（网络接口）。
     */
    private suspend fun loadDanmakuParser(source: BasePlayerSource): cn.a10miaomiao.bilimiao.danmaku.parser.BaseDanmakuParser? {
        // 本地下载文件优先
        val localBytes = (source as? LocalDanmakuSource)?.getLocalDanmakuXmlBytes()
        if (localBytes != null) {
            val dataSource = object : cn.a10miaomiao.bilimiao.danmaku.parser.IDataSource<ByteArray> {
                override fun data() = localBytes
                override fun release() {}
            }
            return cn.a10miaomiao.bilimiao.danmaku.parser.BiliDanmakuParser().apply { load(dataSource) }
        }
        // 回退到网络接口
        return source.getDanmakuParser()
    }

    companion object {
        fun formatTime(ms: Long): String {
            val seconds = ms / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            return if (hours > 0) {
                "%d:%02d:%02d".format(hours, minutes % 60, seconds % 60)
            } else {
                "%02d:%02d".format(minutes % 60, seconds % 60)
            }
        }
    }
}
