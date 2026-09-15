package com.a10miaomiao.bilimiao.comm.delegate.player

import cn.a10miaomiao.bilimiao.danmaku.parser.BaseDanmakuParser
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.mapPreferences
import com.a10miaomiao.bilimiao.comm.entity.player.SubtitleJsonInfo
import com.a10miaomiao.bilimiao.comm.entity.player.toVideoPlayerSource
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import com.a10miaomiao.bilimiao.comm.utils.CompressionTools
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.LocalDanmakuInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlaybackState
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlaybackStatus
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceIds
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceState
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.SubtitleItem
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.SubtitleSourceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.PlaybackState as MediampPlaybackState
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

    /** 字幕内容加载任务（下载字幕 JSON 并解析） */
    private var subtitleLoadJob: Job? = null

    /** 字幕加载请求序号，用于丢弃过期请求（快速切换字幕/切换视频时防止旧结果写回） */
    private var subtitleRequestId = 0

    /** 播放器原生状态订阅协程（mediamp playbackState → PlaybackStatus） */
    private var playbackStateJob: Job? = null

    /** 全屏与屏幕方向控制器 */
    val fullscreenController = FullscreenController(
        scope = coroutineScope,
        isLockScreenOrientationPortraitProvider = isLockScreenOrientationPortraitProvider,
    )

    // 播放参数
    private var quality = 64 // 默认 720P
    private var fnval = 4048 // DASH 格式

    // 播放状态（低频稳定状态，见 PlaybackState）
    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    // 播放源状态（当前播放内容，见 PlayerSourceState）
    private val _sourceState = MutableStateFlow(PlayerSourceState())
    override val sourceState: StateFlow<PlayerSourceState> = _sourceState.asStateFlow()

    // 当前播放位置（高频，约 200ms 更新一次，独立 StateFlow 避免触发无关重组）
    private val _currentPosition = MutableStateFlow(0L)
    override val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    // 本地发送成功的弹幕（供弹幕渲染层本地回显，见 LocalDanmakuInfo）
    private val _localDanmakuFlow = MutableSharedFlow<LocalDanmakuInfo>(extraBufferCapacity = 8)
    override val localDanmakuFlow: SharedFlow<LocalDanmakuInfo> = _localDanmakuFlow.asSharedFlow()

    // 画中画（应用外小窗）状态：由平台层 Activity 回调 onPictureInPictureModeChanged 驱动
    private val _pictureInPicture = MutableStateFlow(false)
    override val pictureInPicture: StateFlow<Boolean> = _pictureInPicture.asStateFlow()

    // 分段播放状态
    private var segmentUrls = listOf<String>()
    private var segmentDurations = listOf<Long>()
    private var currentSegmentIndex = 0
    private var segmentOffsetMs = 0L // 当前段之前的累计时长
    private var segmentHeaders: Map<String, String> = emptyMap()


    override fun createPlayer(): MediampPlayer {
        val player = createMediampPlayer()
        _mediampPlayer = player
        observePlaybackState(player)
        return player
    }

    override fun openPlayer(source: BasePlayerSource) {
        _sourceState.update { it.copy(currentSource = source) }
        playerStore.setPlayerSource(source)
        onShowPlayerChanged?.invoke(true)
        loadAndPlay(source)
        // 检查是否默认全屏播放
        fullscreenController.checkIsPlayerDefaultFull()
    }

    private fun loadAndPlay(source: BasePlayerSource, reloadSubtitle: Boolean = true) {
        val player = _mediampPlayer ?: return
        // 确保播放器状态订阅存在（closePlayer 后重新打开时需重新订阅）
        observePlaybackState(player)
        // 先停止之前的播放
        progressJob?.cancel()
        // 取消并失效旧的字幕加载任务：切换视频时旧任务不能把字幕内容写回；
        // 切换清晰度等场景（reloadSubtitle = false）保留当前字幕不重新加载
        if (reloadSubtitle) {
            subtitleLoadJob?.cancel()
            subtitleRequestId++
        }
        player.stopPlayback()
        coroutineScope.launch {
            _playbackState.update {
                it.copy(
                    status = PlaybackStatus.Loading,
                    errorMessage = null,
                    loadingMessage = "正在加载弹幕...",
                )
            }
            segmentUrls = emptyList()
            segmentDurations = emptyList()
            currentSegmentIndex = 0
            segmentOffsetMs = 0L
            try {
            // 获取弹幕数据：本地下载文件优先读取 danmaku.xml，否则走网络接口
            launch(Dispatchers.IO) {
                try {
                    val parser = loadDanmakuParser(source)
                    _sourceState.update { it.copy(danmakuParser = parser) }
                } catch (e: Exception) {
                    _sourceState.update { it.copy(danmakuParser = null) }
                }
            }

            // 获取 CC 字幕列表并设置默认字幕（切换清晰度时保留当前字幕，不重新加载）
            if (reloadSubtitle) {
                launch(Dispatchers.IO) {
                    try {
                        val subtitles = source.getSubtitles()
                        val defaultSubtitle = selectDefaultSubtitle(subtitles)
                        // 状态更新与字幕内容加载统一回主线程，避免与 UI 的 setSubtitle 并发
                        withContext(Dispatchers.Main) {
                            _sourceState.update {
                                it.copy(
                                    subtitleList = subtitles,
                                    currentSubtitle = defaultSubtitle,
                                    subtitleItems = emptyList(),
                                )
                            }
                            // 自动加载默认字幕内容
                            if (defaultSubtitle != null) {
                                loadSubtitleContent(defaultSubtitle)
                            }
                        }
                    } catch (e: Exception) {
                        _sourceState.update {
                            it.copy(
                                subtitleList = emptyList(),
                                currentSubtitle = null,
                                subtitleItems = emptyList(),
                            )
                        }
                    }
                }
            }

                // 获取播放地址
                _playbackState.update { it.copy(loadingMessage = "正在获取播放地址...") }
                val sourceInfo = source.getPlayerUrl(quality, fnval)
                fullscreenController.playbackInfo = sourceInfo
                _sourceState.update {
                    it.copy(
                        playbackInfo = sourceInfo,
                        currentQuality = sourceInfo.quality,
                    )
                }
                _playbackState.update {
                    it.copy(
                        duration = sourceInfo.duration,
                        loadingMessage = "正在启动播放...",
                    )
                }

                // 解析 URL 格式
                val resolved = resolvePlaybackUrl(sourceInfo.url)

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
                // 后续 Playing/Paused/Buffering 状态由 observePlaybackState 订阅播放器状态驱动
                player.resume()

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
                _playbackState.update {
                    it.copy(
                        status = PlaybackStatus.Error,
                        errorMessage = e.message ?: "播放失败",
                    )
                }
            } finally {
                _playbackState.update { it.copy(loadingMessage = "") }
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

    /**
     * 订阅播放器原生状态，驱动业务播放状态（PlaybackStatus）
     *
     * mediamp 状态机直接上报缓冲等状态（ExoPlayer 的 STATE_BUFFERING、mpv 的 paused-for-cache），
     * 相比原先基于播放位置停滞的轮询推断：零延迟、零误判。
     *
     * 以下状态不在此映射，由业务层控制：
     * - READY / CREATED / DESTROYED：属于业务加载流程（Loading），由 loadAndPlay 维护
     * - FINISHED：由进度轮询的完成检测统一处理（含分段切换与自动连播逻辑）
     */
    private fun observePlaybackState(player: MediampPlayer) {
        playbackStateJob?.cancel()
        playbackStateJob = coroutineScope.launch {
            player.playbackState.collect { state ->
                when (state) {
                    MediampPlaybackState.PLAYING -> setPlaybackStatus(PlaybackStatus.Playing)
                    MediampPlaybackState.PAUSED -> setPlaybackStatus(PlaybackStatus.Paused)
                    MediampPlaybackState.PAUSED_BUFFERING -> setPlaybackStatus(PlaybackStatus.Buffering)
                    MediampPlaybackState.ERROR -> _playbackState.update {
                        it.copy(
                            status = PlaybackStatus.Error,
                            errorMessage = it.errorMessage ?: "播放出错",
                        )
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun setPlaybackStatus(status: PlaybackStatus) {
        if (_playbackState.value.status != status) {
            _playbackState.update { it.copy(status = status) }
        }
    }

    private fun startProgressTracking(source: BasePlayerSource) {
        progressJob?.cancel()
        progressJob = coroutineScope.launch {
            while (isActive) {
                delay(200) // 200ms 更新一次，减少弹幕 wall clock 推进累积误差
                _mediampPlayer?.let { player ->
                    val pos = player.currentPositionMillis.value
                    val position = segmentOffsetMs + pos
                    _currentPosition.value = position

                    // 检测播放完成
                    if (_playbackState.value.duration > 0
                        && position >= _playbackState.value.duration - 1000
                    ) {
                        if (segmentUrls.isNotEmpty() && currentSegmentIndex < segmentUrls.size - 1) {
                            loadNextSegment(player)
                        } else if (_playbackState.value.status != PlaybackStatus.Completed) {
                            _playbackState.update { it.copy(status = PlaybackStatus.Completed) }
                            onAutoCompletion()
                            return@let
                        }
                    }

                    // 检测分段结束
                    if (segmentUrls.isNotEmpty()
                        && currentSegmentIndex < segmentUrls.size - 1
                        && _playbackState.value.status != PlaybackStatus.Completed
                    ) {
                        val segmentDuration = segmentDurations.getOrNull(currentSegmentIndex) ?: 0L
                        if (segmentDuration > 0 && pos >= segmentDuration - 500) {
                            loadNextSegment(player)
                        }
                    }
                }

                // 每5秒上报历史记录
                val position = _currentPosition.value
                if (position % 5000 < 1000) {
                    source.historyReport(position / 1000)
                }
            }
        }
    }

    /**
     * 播放完成后的自动播放逻辑（对齐原安卓版 PlayerController.onAutoCompletion）
     */
    private fun onAutoCompletion() {
        val source = _sourceState.value.currentSource ?: return
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
            // 否则保持 status = Completed 状态，显示播放完成覆盖层
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
            // 状态由 observePlaybackState 订阅播放器实际状态驱动
        }
    }

    override fun resume() {
        _mediampPlayer?.let { player ->
            player.resume()
            // 状态由 observePlaybackState 订阅播放器实际状态驱动
        }
    }

    override fun seekTo(positionMs: Long) {
        _mediampPlayer?.let { player ->
            if (segmentUrls.isNotEmpty()) {
                var accumulated = 0L
                for (i in segmentUrls.indices) {
                    val segDur = segmentDurations.getOrNull(i)?.takeIf { it > 0 }
                        ?: _playbackState.value.duration / segmentUrls.size
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
        _playbackState.update { it.copy(playbackSpeed = speed) }
        _mediampPlayer?.let { player ->
            player.features[PlaybackSpeed]?.set(speed)
        }
    }

    override fun changeQuality(newQuality: Int) {
        val source = _sourceState.value.currentSource ?: return
        quality = newQuality
        val savedPosition = _currentPosition.value
        // 切换清晰度时保留当前字幕（列表/选中/内容），避免字幕闪烁与重复下载
        loadAndPlay(source, reloadSubtitle = false)
        coroutineScope.launch {
            delay(500)
            seekTo(savedPosition)
        }
    }

    override fun toggleDanmaku() {
        _playbackState.update { it.copy(danmakuVisible = !it.danmakuVisible) }
    }

    override fun setSubtitle(subtitle: SubtitleSourceInfo?) {
        _sourceState.update {
            it.copy(
                currentSubtitle = subtitle,
                subtitleItems = emptyList(),
            )
        }
        // 下载并解析所选字幕的内容（null 表示关闭字幕，同时清空内容）
        loadSubtitleContent(subtitle)
    }

    /**
     * 加载字幕内容（对齐原安卓版 PlayerDelegate2.loadSubtitleData 的逻辑）
     *
     * 通过 [subtitleUrl][SubtitleSourceInfo.subtitle_url] 下载 B 站字幕 JSON 并解析为
     * 毫秒时间轴的字幕行（[SubtitleItem]），供 UI 层按播放位置绘制。
     * B 站字幕为私有 JSON 格式，mediamp/ExoPlayer 无法直接解析，故采用自绘方案。
     *
     * 使用 [subtitleRequestId] 校验：快速切换字幕或切换视频时，旧请求完成后的
     * 写入会被丢弃，避免旧视频/旧字幕内容污染当前状态。
     */
    private fun loadSubtitleContent(subtitle: SubtitleSourceInfo?) {
        subtitleLoadJob?.cancel()
        val requestId = ++subtitleRequestId
        if (subtitle == null || subtitle.subtitle_url.isBlank()) {
            _sourceState.update { it.copy(subtitleItems = emptyList()) }
            return
        }
        subtitleLoadJob = coroutineScope.launch(Dispatchers.IO) {
            try {
                val res = MiaoHttp.request {
                    url = UrlUtil.autoHttps(subtitle.subtitle_url)
                }.awaitCall().json<SubtitleJsonInfo>()
                val items = res.body.map {
                    SubtitleItem(
                        from = (it.from * 1000).toLong(),
                        to = (it.to * 1000).toLong(),
                        content = it.content,
                    )
                }
                if (requestId == subtitleRequestId) {
                    _sourceState.update { it.copy(subtitleItems = items) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                if (requestId == subtitleRequestId) {
                    _sourceState.update { it.copy(subtitleItems = emptyList()) }
                }
            }
        }
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
        _playbackState.update { it.copy(volume = clamped) }
        _mediampPlayer?.let { player ->
            setPlayerVolume(player, clamped)
        }
    }

    override fun replay() {
        seekTo(0)
        resume()
    }

    override fun playNext() {
        val source = _sourceState.value.currentSource ?: return
        val next = source.next() ?: return
        openPlayer(next)
    }

    override fun retry() {
        val source = _sourceState.value.currentSource ?: return
        loadAndPlay(source)
    }

    override fun closePlayer() {
        progressJob?.cancel()
        playbackStateJob?.cancel()
        // 取消字幕加载任务并使其失效，防止完成后把字幕内容写回
        subtitleLoadJob?.cancel()
        subtitleRequestId++
        _mediampPlayer?.let { player ->
            player.stopPlayback()
        }
        _sourceState.value.danmakuParser?.release()
        _playbackState.update {
            it.copy(
                status = PlaybackStatus.Idle,
                duration = 0L,
                errorMessage = null,
                loadingMessage = "",
            )
        }
        _sourceState.update {
            it.copy(
                currentSource = null,
                playbackInfo = null,
                danmakuParser = null,
                subtitleList = emptyList(),
                currentSubtitle = null,
                subtitleItems = emptyList(),
            )
        }
        _currentPosition.value = 0L
        segmentUrls = emptyList()
        segmentDurations = emptyList()
        currentSegmentIndex = 0
        segmentOffsetMs = 0L
        playerStore.clearPlayerInfo()
        fullscreenController.smallScreen()
        onShowPlayerChanged?.invoke(false)
    }

    override fun currentPosition(): Long = _currentPosition.value

    override fun isPlaying(): Boolean = _playbackState.value.status == PlaybackStatus.Playing

    override fun isPause(): Boolean = _playbackState.value.status == PlaybackStatus.Paused

    override fun isOpened(): Boolean = _sourceState.value.currentSource != null

    override fun getSourceIds(): PlayerSourceIds {
        return _sourceState.value.currentSource?.getSourceIds() ?: PlayerSourceIds()
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

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        _pictureInPicture.value = isInPictureInPictureMode
    }
    override fun setWindowInsets(left: Int, top: Int, right: Int, bottom: Int) {}
    override fun onConfigurationChanged(orientation: Int) {}

    override fun sendDanmaku(
        type: Int,
        danmakuText: String,
        danmakuTextSize: Float,
        danmakuTextColor: Int,
        danmakuPosition: Long
    ) {
        // 本地回显：派发给弹幕渲染层，由其创建弹幕（带边框）加入弹幕引擎
        _localDanmakuFlow.tryEmit(
            LocalDanmakuInfo(
                type = type,
                text = danmakuText,
                textSize = danmakuTextSize,
                textColor = danmakuTextColor,
                position = danmakuPosition,
            )
        )
        // 对齐旧版 PlayerDelegate2.sendDanmaku：暂停状态发送后恢复播放，
        // 否则弹幕引擎计时器不推进，本地回显的弹幕不会显示/移动
        if (!isPlaying()) {
            resume()
        }
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
