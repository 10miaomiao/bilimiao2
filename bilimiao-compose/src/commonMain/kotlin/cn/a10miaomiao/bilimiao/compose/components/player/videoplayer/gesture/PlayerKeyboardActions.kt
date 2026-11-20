package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.nextPlaybackSpeed
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegateImpl
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlaybackStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** 非 Shift 精细调整时的音量步长 (±10%, 与手势滑动的粗调档位一致). */
private const val VOLUME_STEP = 0.10f

/** 再次按 M 恢复音量时, 没有记忆值可恢复则用 50%. */
private const val VOLUME_RESTORE_FALLBACK = 0.5f

/**
 * 播放器键盘快捷键的动作实现.
 *
 * 原先是 `BiliVideoScaffold` 里内联构造的一堆 lambda: 每次重组 (播放进度、缓冲状态等
 * 都会触发) 都重新 `new` 一个 [PlayerKeyboardShortcutCallbacks], lambda 无法相等,
 * 实例恒变, 手势层的键盘修饰符因此拿不到稳定引用。
 *
 * 这里把回调与相关的小状态收进一个类, 由 [rememberPlayerKeyboardActions] `remember`
 * 一次, 播放期间引用恒定:
 * - 实时数据 (播放状态 / 进度 / 倍速 / 音量) 一律在回调执行时从 [playerDelegate] 与
 *   [audioController] 的 `StateFlow.value` / 属性读取, 不在构造期捕获快照;
 * - 只有「需要触发重组的 UI 状态」(帮助浮层显隐 [helpVisible]) 用 Compose 状态持有.
 *
 * @param playerDelegate 播放器代理, 提供播放状态、进度、seek、播放控制能力
 * @param audioController 音量控制器 (安卓为系统媒体音量, 桌面回退到播放器音量)
 * @param gestureIndicatorState 手势提示指示器, 音量 / 倍速变化时给出提示
 * @param scope 用于显示指示器的协程作用域, 由 remember 工厂提供
 */
@Stable
class PlayerKeyboardActions internal constructor(
    private val playerDelegate: PlayerDelegateImpl,
    private val audioController: LevelController,
    private val gestureIndicatorState: GestureIndicatorState,
    private val scope: CoroutineScope,
) {
    /** 快捷键帮助浮层是否可见（H / ? 切换）。 */
    var helpVisible: Boolean by mutableStateOf(false)
        private set

    /**
     * 由 remember 工厂在每帧同步的最新参数。
     *
     * lambda 参数每次重组都可能换新实例, 不能作为 `remember` 的 key, 否则本节点的稳定实例
     * 会被破坏 (这正是本次要修的问题); 改为用 [SideEffect] 写入字段, 回调执行时读最新值.
     */
    internal var toggleFullscreen: () -> Unit = {}

    /** 退出全屏 (Esc 用), 见 [toggleFullscreen]. */
    internal var exitFullscreen: () -> Unit = {}

    /** 当前是否处于全屏, 见 [toggleFullscreen]. */
    internal var isFullscreen: () -> Boolean = { false }

    /** 静音前的音量（-1f 表示没有记录），再按一次恢复。 */
    private var volumeBeforeMute = -1f

    /**
     * A / D / S 的倍速区间: 默认区间之外放宽。
     *
     * 不能直接用默认的 0.5x..2.5x: 当前倍速已是 3x / 4x 时, 按 D 会被夹回 2.5x (反而降速)。
     * 这里取 0.25x..max(4x, 当前倍速), 覆盖常见区间的同时保证上限不低于当前倍速。
     */
    private fun speedRange(current: Float): ClosedFloatingPointRange<Float> =
        0.25f..maxOf(4f, current)

    /** 显示音量指示器 (音量变化后给出与手势滑动一致的提示)。 */
    private fun showVolumeIndicator() {
        scope.launch { gestureIndicatorState.showVolumeRange(audioController.level) }
    }

    /**
     * 传给手势层的快捷键回调; 实例在 remember 期间恒定。
     *
     * 所有条目均直接读播放器实时状态, 不依赖调用方每帧重传参数。
     */
    val callbacks: PlayerKeyboardShortcutCallbacks = PlayerKeyboardShortcutCallbacks(
        // 相对 seek: 目标 = 当前位置 + 相对秒数, 钳制到 [0, 片长]。
        // 键盘 seek 直接提交给播放器, 不走进度条的 preview → finishPreview 流程:
        // 那条路径会先 onPreview 再 onPreviewFinished, 同一个目标会被 seek 两次,
        // 且 displayPositionRatio 会先跳到目标再回落一下 (视觉闪烁)。
        onSeekBySeconds = { seconds ->
            val total = playerDelegate.playbackState.value.duration
            if (total > 0L) {
                val target = (playerDelegate.currentPosition.value + seconds * 1000L)
                    .coerceIn(0L, total)
                playerDelegate.seekTo(target)
            }
        },
        // 绝对 seek: 按片长百分比跳转 (Home / End / 数字键), 同样直接 seek 播放器
        onSeekToFraction = { fraction ->
            val total = playerDelegate.playbackState.value.duration
            if (total > 0L) {
                val target = (total * fraction).toLong().coerceIn(0L, total)
                playerDelegate.seekTo(target)
            }
        },
        // 播放 / 暂停: 以实时状态判断当前是否在播, 不用组合期快照
        onTogglePauseResume = {
            if (playerDelegate.playbackState.value.status == PlaybackStatus.Playing) {
                playerDelegate.pause()
            } else {
                playerDelegate.resume()
            }
        },
        onToggleFullscreen = { toggleFullscreen() },
        // Esc 只用于「退出全屏」: 对外参数 onExitFullscreen 默认等于 onToggleFullscreen,
        // 非全屏时直接转发会让 Esc 变成「进入全屏」, 因此这里按当前状态收口
        onExitFullscreen = { if (isFullscreen()) exitFullscreen() },
        onToggleDanmaku = { playerDelegate.toggleDanmaku() },
        // 静音切换: > 0 时记住当前音量并静音, 为 0 时恢复到记住的音量 (无记忆则 50%)
        onToggleMute = {
            val currentLevel = audioController.level
            if (currentLevel > 0f) {
                volumeBeforeMute = currentLevel
                audioController.setLevel(0f)
            } else {
                audioController.setLevel(
                    if (volumeBeforeMute > 0f) volumeBeforeMute else VOLUME_RESTORE_FALLBACK
                )
            }
            showVolumeIndicator()
        },
        // 音量与手势同款: Shift 精细调整 (一个 levelStep), 否则 ±10%
        onVolumeUp = { fine ->
            audioController.increaseLevel(if (fine) audioController.levelStep else VOLUME_STEP)
            showVolumeIndicator()
        },
        onVolumeDown = { fine ->
            audioController.decreaseLevel(if (fine) audioController.levelStep else VOLUME_STEP)
            showVolumeIndicator()
        },
        onToggleKeyboardHelp = { helpVisible = !helpVisible },
        // 倍速步进: 基准值取播放器当前实际倍速 (倍速菜单 / 长按加速都可能改过它)
        onPlaybackSpeedStep = { direction ->
            val current = playerDelegate.playbackState.value.playbackSpeed
            val range = speedRange(current)
            val target = if (direction == 0) {
                1f.coerceIn(range)
            } else {
                nextPlaybackSpeed(current, range, direction)
            }
            playerDelegate.setPlaybackSpeed(target)
            scope.launch { gestureIndicatorState.showPlaybackSpeed(target) }
        },
    )

    /** 关闭帮助浮层。 */
    fun dismissHelp() {
        helpVisible = false
    }
}

/**
 * 创建并记住 [PlayerKeyboardActions].
 *
 * `remember` 的 key 只包含播放期间不变的对象 ([playerDelegate] / [audioController] /
 * [gestureIndicatorState]); 三个 lambda 参数每次重组都可能换新实例, 因此不作为 key,
 * 而是用 [SideEffect] 写进字段。这样 [PlayerKeyboardActions] 及其
 * [callbacks][PlayerKeyboardActions.callbacks] 在整段播放中保持同一引用, 手势层的
 * 键盘修饰符不会因为播放进度 / 缓冲状态变化而反复丢弃重建。
 *
 * @param playerDelegate 播放器代理
 * @param audioController 音量控制器
 * @param gestureIndicatorState 手势提示指示器
 * @param onToggleFullscreen 切换全屏
 * @param onExitFullscreen 退出全屏
 * @param isFullscreen 当前是否全屏 (Esc 时据此决定是否真的退出)
 */
@Composable
fun rememberPlayerKeyboardActions(
    playerDelegate: PlayerDelegateImpl,
    audioController: LevelController,
    gestureIndicatorState: GestureIndicatorState,
    onToggleFullscreen: () -> Unit,
    onExitFullscreen: () -> Unit,
    isFullscreen: () -> Boolean,
): PlayerKeyboardActions {
    val scope = rememberCoroutineScope()
    val actions = remember(playerDelegate, audioController, gestureIndicatorState) {
        PlayerKeyboardActions(playerDelegate, audioController, gestureIndicatorState, scope)
    }
    SideEffect {
        actions.toggleFullscreen = onToggleFullscreen
        actions.exitFullscreen = onExitFullscreen
        actions.isFullscreen = isFullscreen
    }
    return actions
}
