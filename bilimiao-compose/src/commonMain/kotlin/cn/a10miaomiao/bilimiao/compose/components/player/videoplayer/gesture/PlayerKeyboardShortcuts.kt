@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.nextPlaybackSpeed

/**
 * 在单个拥有焦点的目标上安装播放器键盘命令.
 *
 * 调用方负责焦点策略. 只有当被修饰的节点拥有焦点时命令才生效, 因此文本框或其他播放器控件
 * 可以临时接管键盘输入而不触发播放命令.
 *
 * 替代原 animeko 的 `onKey`: 所有快捷键在 [KeyEventType.KeyUp] 时触发.
 */
internal fun Modifier.playerKeyboardShortcuts(
    seekerState: SwipeSeekerState,
    fastSkipState: FastSkipState?,
    currentPlaybackSpeed: Float?,
    playbackSpeedRange: ClosedFloatingPointRange<Float>,
    onPlaybackSpeedChanged: (Float) -> Unit,
    volumeEnabled: Boolean,
    onVolumeUp: (fineAdjustment: Boolean) -> Unit,
    onVolumeDown: (fineAdjustment: Boolean) -> Unit,
    onTogglePauseResume: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onExitFullscreen: () -> Unit,
    onToggleDanmaku: () -> Unit,
    onTogglePlayerStats: () -> Unit,
): Modifier {
    var result = keyboardSeekAndFastForward(
        onSeekBackward = { seekerState.onSeek(-5) },
        onSeekForward = { seekerState.onSeek(5) },
        fastSkipState = fastSkipState,
    )
    if (volumeEnabled) {
        result = result.onKeyEvent { event ->
            if (event.type == KeyEventType.KeyUp) {
                when (event.key) {
                    Key.DirectionUp -> {
                        onVolumeUp(event.isShiftPressed)
                        true
                    }

                    Key.DirectionDown -> {
                        onVolumeDown(event.isShiftPressed)
                        true
                    }

                    else -> false
                }
            } else {
                false
            }
        }
    }
    // 替代原 animeko 的 onKey 链: 统一在 KeyUp 时触发
    result = result.onKeyEvent { event ->
        if (event.type != KeyEventType.KeyUp) return@onKeyEvent false
        when (event.key) {
            Key.Spacebar -> {
                onTogglePauseResume()
                true
            }

            Key.Escape -> {
                onExitFullscreen()
                true
            }

            Key.F -> {
                onToggleFullscreen()
                true
            }

            else -> false
        }
    }
    if (currentPlaybackSpeed != null) {
        result = result.onKeyEvent { event ->
            if (event.type != KeyEventType.KeyUp) return@onKeyEvent false
            when (event.key) {
                Key.A -> {
                    onPlaybackSpeedChanged(nextPlaybackSpeed(currentPlaybackSpeed, playbackSpeedRange, -1))
                    true
                }

                Key.D -> {
                    onPlaybackSpeedChanged(nextPlaybackSpeed(currentPlaybackSpeed, playbackSpeedRange, 1))
                    true
                }

                Key.S -> {
                    onPlaybackSpeedChanged(1f.coerceIn(playbackSpeedRange))
                    true
                }

                else -> false
            }
        }
    }
    result = result.onKeyEvent { event ->
        if (event.type != KeyEventType.KeyUp) return@onKeyEvent false
        when (event.key) {
            Key.B -> {
                onToggleDanmaku()
                true
            }

            Key.I -> {
                onTogglePlayerStats()
                true
            }

            else -> false
        }
    }
    // 同一节点携带 combinedClickable, 获得焦点时会把 Enter 当作点击.
    // Enter 不是播放器快捷键, 故吞掉; DPad 中心留给 clickable, 使遥控/DPad 激活仍像点击一样工作.
    return result
        .onPreviewKeyEvent { event ->
            event.key == Key.Enter || event.key == Key.NumPadEnter
        }
}
