package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * 播放器键盘快捷键的动作回调集合.
 *
 * 可选回调为 `null` 时对应按键完全不注册: 事件原样放行, 不会被播放器消费.
 *
 * 本类由调用方 `remember` 一次并在播放期间保持同一引用 (见 `PlayerKeyboardActions.callbacks`),
 * 因此只需要 Composable 意义上的「稳定」: 全部属性为 `val`, 不含可变状态。
 * 不再使用 `data class`: 实例只创建一次, 逐字段构造出来的 `equals` / `copy` 没有用武之地。
 *
 * @param onSeekBySeconds 相对 seek, 参数为带符号的相对秒数 (后退为负, 前进为正)
 * @param onSeekToFraction 绝对 seek, 参数为片长百分比 `0f..1f`
 * @param onTogglePauseResume 播放 / 暂停切换
 * @param onToggleFullscreen 切换全屏
 * @param onExitFullscreen 退出全屏
 * @param onToggleDanmaku 切换弹幕显示
 * @param onToggleMute 静音切换, `null` 时 M 键放行
 * @param onVolumeUp 音量增加, 参数 `fine` 为 `true` 表示精细调整 (按住 Shift)
 * @param onVolumeDown 音量减少, 参数 `fine` 为 `true` 表示精细调整 (按住 Shift)
 * @param onToggleKeyboardHelp 显示 / 隐藏快捷键帮助浮层, `null` 时 H / ? 键放行
 * @param onPlaybackSpeedStep 倍速步进: `-1` 减速一档, `+1` 加速一档, `0` 复位 1x;
 * `null` 时 A / D / S 键放行
 */
@Stable
class PlayerKeyboardShortcutCallbacks(
    val onSeekBySeconds: (seconds: Int) -> Unit,
    val onSeekToFraction: (fraction: Float) -> Unit,
    val onTogglePauseResume: () -> Unit,
    val onToggleFullscreen: () -> Unit,
    val onExitFullscreen: () -> Unit,
    val onToggleDanmaku: () -> Unit,
    val onToggleMute: (() -> Unit)? = null,
    val onVolumeUp: ((fine: Boolean) -> Unit)? = null,
    val onVolumeDown: ((fine: Boolean) -> Unit)? = null,
    val onToggleKeyboardHelp: (() -> Unit)? = null,
    val onPlaybackSpeedStep: ((direction: Int) -> Unit)? = null,
)

/** 数字键 1-9 对应 10%..90%, 0 对应 0%, 其它键返回 `null`. */
private fun keyboardSeekFraction(key: Key): Float? = when (key) {
    Key.Zero -> 0f
    Key.One -> 0.1f
    Key.Two -> 0.2f
    Key.Three -> 0.3f
    Key.Four -> 0.4f
    Key.Five -> 0.5f
    Key.Six -> 0.6f
    Key.Seven -> 0.7f
    Key.Eight -> 0.8f
    Key.Nine -> 0.9f
    else -> null
}

/**
 * 在手势层安装播放器键盘快捷键.
 *
 * 挂载点是手势层那个带 `.focusable()` 的 Box: 桌面端点画面时 `combinedClickable`
 * 会把焦点交给该节点, 快捷键随之生效 (顶部 / 底部控制器不在手势层子树内,
 * 点控制器按钮后焦点会离开手势层, 此时按键不再被播放器消费).
 *
 * 普通快捷键统一在 [KeyEventType.KeyUp] 触发, 避免按住时重复执行;
 * 左右方向键需要在 [KeyEventType.KeyDown] 就消费事件, 以支撑长按快进 (见 [keyboardSeekAndFastForward]).
 *
 * @param seekerState 屏幕横滑 seek 状态, 与手势共用: 手指正在拖动进度时键盘 seek 与之互斥
 * @param fastSkipState 长按方向键快进使用的快进状态, 与长按手势共用
 * @param callbacks 各快捷键的动作回调
 */
internal fun Modifier.playerKeyboardShortcuts(
    seekerState: SwipeSeekerState,
    fastSkipState: FastSkipState?,
    callbacks: PlayerKeyboardShortcutCallbacks,
): Modifier = composed {
    // 键盘修饰符在整个播放期间存活, 直接捕获参数会让事件读到组合初期的过期状态,
    // 因此所有可变依赖都经 rememberUpdatedState 读取最新值 (只在事件里读, 不引发重组).
    val latestCallbacks by rememberUpdatedState(callbacks)
    val latestSeekerState by rememberUpdatedState(seekerState)
    val latestFastSkipState by rememberUpdatedState(fastSkipState)

    // 左右方向键 seek: 长按右方向键 200ms 后进入快进, 松手停止 (与滑动手势共用 FastSkipState).
    // 唯一的互斥条件是手指正在滑动进度 (seekerState.isSeeking), 避免一边拖动一边被键盘改写进度.
    keyboardSeekAndFastForward(
        onSeekBackward = { seconds -> latestCallbacks.onSeekBySeconds(-seconds) },
        onSeekForward = { seconds -> latestCallbacks.onSeekBySeconds(seconds) },
        fastSkipState = latestFastSkipState,
        isEnabled = { !latestSeekerState.isSeeking },
    ).onPreviewKeyEvent { event ->
        val shortcutCallbacks = latestCallbacks
        // 普通快捷键统一在 KeyUp 触发; 左右方向键已在 keyboardSeekAndFastForward 中处理
        if (event.type != KeyEventType.KeyUp) {
            return@onPreviewKeyEvent false
        }
        val fineAdjustment = event.isShiftPressed
        when (event.key) {
            // 播放 / 暂停
            Key.Spacebar, Key.K -> {
                shortcutCallbacks.onTogglePauseResume()
                true
            }

            // 音量: 按住 Shift 为精细调整 (±1%), 否则 ±10%.
            // 两个音量回调必须成对存在: 只能调大不能调小会让用户以为按键失效, 故一起放行.
            Key.DirectionUp -> {
                val onVolumeUp = shortcutCallbacks.onVolumeUp
                if (onVolumeUp == null || shortcutCallbacks.onVolumeDown == null) {
                    return@onPreviewKeyEvent false
                }
                onVolumeUp(fineAdjustment)
                true
            }

            Key.DirectionDown -> {
                val onVolumeDown = shortcutCallbacks.onVolumeDown
                if (onVolumeDown == null || shortcutCallbacks.onVolumeUp == null) {
                    return@onPreviewKeyEvent false
                }
                onVolumeDown(fineAdjustment)
                true
            }

            // 进度: 相对秒数
            Key.PageUp -> {
                // 键位表语义是「前进 30 秒」, 相对秒数无法用比例表达, 故走 onSeekBySeconds
                shortcutCallbacks.onSeekBySeconds(KEYBOARD_SEEK_LARGE_SECONDS)
                true
            }

            Key.PageDown -> {
                shortcutCallbacks.onSeekBySeconds(-KEYBOARD_SEEK_LARGE_SECONDS)
                true
            }

            // 进度: 比例跳转
            Key.MoveHome -> {
                shortcutCallbacks.onSeekToFraction(0f)
                true
            }

            Key.MoveEnd -> {
                shortcutCallbacks.onSeekToFraction(1f)
                true
            }

            // 倍速: 只表达「方向」, 具体目标倍速由回调内部按当前实际倍速推算,
            // 保证 A / D / S 与倍速菜单、长按加速改过的倍速始终一致
            Key.A -> {
                val onPlaybackSpeedStep = shortcutCallbacks.onPlaybackSpeedStep
                    ?: return@onPreviewKeyEvent false
                onPlaybackSpeedStep(-1)
                true
            }

            Key.D -> {
                val onPlaybackSpeedStep = shortcutCallbacks.onPlaybackSpeedStep
                    ?: return@onPreviewKeyEvent false
                onPlaybackSpeedStep(1)
                true
            }

            Key.S -> {
                val onPlaybackSpeedStep = shortcutCallbacks.onPlaybackSpeedStep
                    ?: return@onPreviewKeyEvent false
                onPlaybackSpeedStep(0)
                true
            }

            // 画面
            Key.F -> {
                shortcutCallbacks.onToggleFullscreen()
                true
            }

            Key.Escape -> {
                shortcutCallbacks.onExitFullscreen()
                true
            }

            Key.B -> {
                shortcutCallbacks.onToggleDanmaku()
                true
            }

            // 静音
            Key.M -> {
                val onToggleMute = shortcutCallbacks.onToggleMute ?: return@onPreviewKeyEvent false
                onToggleMute()
                true
            }

            // 快捷键帮助: "?" 在美式布局上是 Shift + /, 这里同时接受 H 与 /
            Key.H, Key.Slash -> {
                val onToggleHelp = shortcutCallbacks.onToggleKeyboardHelp
                    ?: return@onPreviewKeyEvent false
                onToggleHelp()
                true
            }

            // 数字键 1-9 跳到 10%..90%, 0 跳到开头
            else -> {
                val fraction = keyboardSeekFraction(event.key)
                    ?: return@onPreviewKeyEvent false
                shortcutCallbacks.onSeekToFraction(fraction)
                true
            }
        }
    }
}
