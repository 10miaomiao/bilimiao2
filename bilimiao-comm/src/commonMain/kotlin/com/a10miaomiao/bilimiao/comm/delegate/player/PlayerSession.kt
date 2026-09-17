package com.a10miaomiao.bilimiao.comm.delegate.player

import com.a10miaomiao.bilimiao.comm.delegate.player.entity.LocalDanmakuInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlaybackState
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlaybackStatus
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.PlaybackState as MediampPlaybackState
import org.openani.mediamp.source.UriMediaData

/**
 * 播放会话：跨界面存活的播放器实例与播放状态
 *
 * ## 为什么需要它
 *
 * 安卓端 [PlayerDelegateImpl] 由 Activity 创建（`MainActivity.basePlayerDelegate`）。
 * 若把播放器与播放状态也放在其中，Activity 一旦重建（切换深色模式、修改屏幕密度、
 * 系统回收、开发者选项「不保留活动」）就会：
 * 1. 新的 delegate 重新创建 ExoPlayer → 旧实例无人释放 → **双路音频**；
 * 2. 旧 delegate 的协程被取消 → 进度上报、自动连播停摆，但音频仍在播放；
 * 3. 新 delegate 的播放状态为空 → 界面与正在播放的音频**脱节**。
 *
 * 因此把「播放会话」抽到进程级单例（[shared]），由它持有播放器实例、状态流与
 * 长期存活的协程；[PlayerDelegateImpl] 退化为无状态门面，Activity 重建只是重新
 * 接线，播放本身不受影响。这也正是后台播放的前提：播放器必须活得比界面久。
 *
 * ## 边界
 *
 * 本类只持有播放器与纯播放状态，**不得**持有 DI 容器、Store、Activity 或任何界面
 * 作用域对象，否则会把已销毁的 Activity 一并泄漏。需要 Store 的能力（如播放完成后
 * 自动连播）通过 [onPlaybackCompleted] 回调交给当前存活的 [PlayerDelegateImpl]，
 * 并在其销毁时注销。
 */
class PlayerSession {

    /** 当前播放器实例（进程内唯一，由 [PlayerDelegateImpl.createPlayer] 创建一次） */
    var mediampPlayer: MediampPlayer? = null

    /** 播放状态（低频稳定状态，见 [PlaybackState]） */
    val playbackStateFlow = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = playbackStateFlow.asStateFlow()

    /** 播放源状态（当前播放内容，见 [PlayerSourceState]） */
    val sourceStateFlow = MutableStateFlow(PlayerSourceState())
    val sourceState: StateFlow<PlayerSourceState> = sourceStateFlow.asStateFlow()

    /** 当前播放位置（高频，约 200ms 更新一次，独立 StateFlow 避免触发无关重组） */
    val currentPositionFlow = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = currentPositionFlow.asStateFlow()

    /** 本地发送成功的弹幕（供弹幕渲染层本地回显，见 [LocalDanmakuInfo]） */
    val localDanmakuFlowShared = MutableSharedFlow<LocalDanmakuInfo>(extraBufferCapacity = 8)
    val localDanmakuFlow: SharedFlow<LocalDanmakuInfo> = localDanmakuFlowShared.asSharedFlow()

    /** 画中画（应用外小窗）状态：由平台层 Activity 回调驱动 */
    val pictureInPictureFlow = MutableStateFlow(false)
    val pictureInPicture: StateFlow<Boolean> = pictureInPictureFlow.asStateFlow()

    /**
     * 播放会话协程作用域：与进程同寿命。
     *
     * **不随 Activity 销毁而取消**，否则后台播放期间进度上报、分段切换、播放完成检测
     * 都会停摆。协程体内不得捕获 [PlayerDelegateImpl]（否则会经它持有 Activity）。
     */
    val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /** 进度跟踪任务（长期存活，见 [startProgressTracking]） */
    var progressJob: Job? = null

    /** 播放器状态订阅任务（长期存活，见 [observePlaybackState]） */
    var playbackStateJob: Job? = null

    /** 字幕内容加载任务（短任务） */
    var subtitleLoadJob: Job? = null

    /** 字幕加载请求序号，用于丢弃过期请求 */
    var subtitleRequestId = 0

    // 播放参数（清晰度切换需要跨界面保留）
    var quality = 64 // 默认 720P
    var fnval = 4048 // DASH 格式

    // 分段播放状态（分段视频播放中需要跨界面保留）
    var segmentUrls: List<String> = emptyList()
    var segmentDurations: List<Long> = emptyList()
    var currentSegmentIndex = 0
    var segmentOffsetMs = 0L
    var segmentHeaders: Map<String, String> = emptyMap()

    /**
     * 播放完成回调（自动连播等需要播放列表 Store 的逻辑）
     *
     * 由当前存活的 [PlayerDelegateImpl] 注册，在其销毁时注销：Session 不持有界面作用域
     * 的对象，因此后台（界面已销毁）播放完成时不会触发自动连播，但也不会泄漏 Activity。
     */
    var onPlaybackCompleted: (() -> Unit)? = null

    /**
     * 订阅播放器原生状态，驱动业务播放状态（[PlaybackStatus]）
     *
     * mediamp 状态机直接上报缓冲等状态（ExoPlayer 的 STATE_BUFFERING、mpv 的 paused-for-cache），
     * 相比基于播放位置停滞的轮询推断：零延迟、零误判。
     *
     * 以下状态不在此映射，由业务层控制：
     * - READY / CREATED / DESTROYED：属于业务加载流程（Loading），由 loadAndPlay 维护
     * - FINISHED：由进度轮询的完成检测统一处理（含分段切换与自动连播逻辑）
     */
    fun observePlaybackState(player: MediampPlayer) {
        playbackStateJob?.cancel()
        playbackStateJob = coroutineScope.launch {
            player.playbackState.collect { state ->
                when (state) {
                    MediampPlaybackState.PLAYING -> setPlaybackStatus(PlaybackStatus.Playing)
                    MediampPlaybackState.PAUSED -> setPlaybackStatus(PlaybackStatus.Paused)
                    MediampPlaybackState.PAUSED_BUFFERING -> setPlaybackStatus(PlaybackStatus.Buffering)
                    MediampPlaybackState.ERROR -> playbackStateFlow.update {
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
        if (playbackStateFlow.value.status != status) {
            playbackStateFlow.update { it.copy(status = status) }
        }
    }

    /**
     * 播放进度跟踪（约 200ms 一次）
     *
     * 负责：播放位置上报、分段视频切换、播放完成检测与通知、播放历史上报。
     * 任务归 Session 所有（长期存活），因此界面销毁重建不会中断，后台播放仍能持续上报。
     */
    fun startProgressTracking(source: BasePlayerSource) {
        progressJob?.cancel()
        progressJob = coroutineScope.launch {
            while (isActive) {
                delay(200) // 200ms 更新一次，减少弹幕 wall clock 推进累积误差
                mediampPlayer?.let { player ->
                    val pos = player.currentPositionMillis.value
                    val position = segmentOffsetMs + pos
                    currentPositionFlow.value = position

                    // 检测播放完成
                    if (playbackStateFlow.value.duration > 0
                        && position >= playbackStateFlow.value.duration - 1000
                    ) {
                        if (segmentUrls.isNotEmpty() && currentSegmentIndex < segmentUrls.size - 1) {
                            loadNextSegment(player)
                        } else if (playbackStateFlow.value.status != PlaybackStatus.Completed) {
                            playbackStateFlow.update { it.copy(status = PlaybackStatus.Completed) }
                            onPlaybackCompleted?.invoke()
                            return@let
                        }
                    }

                    // 检测分段结束
                    if (segmentUrls.isNotEmpty()
                        && currentSegmentIndex < segmentUrls.size - 1
                        && playbackStateFlow.value.status != PlaybackStatus.Completed
                    ) {
                        val segmentDuration = segmentDurations.getOrNull(currentSegmentIndex) ?: 0L
                        if (segmentDuration > 0 && pos >= segmentDuration - 500) {
                            loadNextSegment(player)
                        }
                    }
                }

                // 每5秒上报历史记录
                val position = currentPositionFlow.value
                if (position % 5000 < 1000) {
                    source.historyReport(position / 1000)
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

    companion object {
        /**
         * 进程级共享会话
         *
         * 安卓端由 MainActivity 持有的 [PlayerDelegateImpl] 反复创建，但会话只有这一个，
         * 从而保证播放器实例与播放状态跨 Activity 重建存活。
         */
        val shared: PlayerSession by lazy { PlayerSession() }
    }
}
