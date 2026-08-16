@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 键盘水平方向状态, 持有左右方向键的回调.
 */
@Stable
class KeyboardHorizontalDirectionState(
    val onBackward: () -> Unit,
    val onForward: () -> Unit,
)

/**
 * 响应键盘左右方向键.
 *
 * 根据布局方向 (LTR/RTL) 映射前后方向键.
 */
fun Modifier.onKeyboardHorizontalDirection(
    state: KeyboardHorizontalDirectionState,
): Modifier = onKeyboardHorizontalDirection(
    onBackward = state.onBackward,
    onForward = state.onForward,
)

/**
 * 响应键盘左右方向键.
 *
 * 根据布局方向 (LTR/RTL) 映射前后方向键.
 */
fun Modifier.onKeyboardHorizontalDirection(
    onBackward: () -> Unit,
    onForward: () -> Unit,
): Modifier = composed {
    val layoutDirection = LocalLayoutDirection.current
    val backwardKey = if (layoutDirection == LayoutDirection.Ltr) {
        Key.DirectionLeft
    } else {
        Key.DirectionRight
    }
    val forwardKey = if (layoutDirection == LayoutDirection.Ltr) {
        Key.DirectionRight
    } else {
        Key.DirectionLeft
    }

    val onBackwardState by rememberUpdatedState(onBackward)
    val onForwardState by rememberUpdatedState(onForward)
    // 替代原 animeko 的 onKey: 手动实现 KeyUp 时触发
    onPreviewKeyEvent { event ->
        if (event.type == KeyEventType.KeyUp) {
            when (event.key) {
                backwardKey -> {
                    onBackwardState()
                    true
                }

                forwardKey -> {
                    onForwardState()
                    true
                }

                else -> false
            }
        } else {
            false
        }
    }
}

/**
 * 键盘 seek 与长按快进.
 *
 * - 短按左右方向键: 触发 [onSeekBackward] / [onSeekForward]
 * - 长按右方向键 (200ms 后): 触发 [fastSkipState] 快进; 松手时若已进入快进则停止, 否则视为短按 seek
 *
 * 替代原 animeko 的 `rememberUiMonoTasker` 为 `rememberCoroutineScope` + 单任务 [Job] 管理.
 */
fun Modifier.keyboardSeekAndFastForward(
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    fastSkipState: FastSkipState?,
): Modifier = composed {
    val layoutDirection = LocalLayoutDirection.current
    val backwardKey = if (layoutDirection == LayoutDirection.Ltr) {
        Key.DirectionLeft
    } else {
        Key.DirectionRight
    }
    val forwardKey = if (layoutDirection == LayoutDirection.Ltr) {
        Key.DirectionRight
    } else {
        Key.DirectionLeft
    }

    val onBackwardState by rememberUpdatedState(onSeekBackward)
    val onForwardState by rememberUpdatedState(onSeekForward)
    val scope = rememberCoroutineScope()
    var seekJob by remember { mutableStateOf<Job?>(null) }
    var ticket by remember { mutableStateOf<Int?>(null) }

    onPreviewKeyEvent { event ->
        if (event.key == backwardKey) {
            if (event.type == KeyEventType.KeyDown) {
                // 消费 KeyDown, 等待 KeyUp 再 seek
                true
            } else if (event.type == KeyEventType.KeyUp) {
                onBackwardState()
                true
            } else {
                false
            }
        } else if (event.key == forwardKey) {
            if (event.type == KeyEventType.KeyDown) {
                // 若没有正在进行的快进任务, 启动一个: 200ms 后进入快进
                if (seekJob == null) {
                    seekJob = scope.launch {
                        try {
                            delay(200)
                            fastSkipState?.let {
                                ticket = it.startSkipping(SkipDirection.FORWARD)
                            }
                            awaitCancellation()
                        } finally {
                            ticket?.let {
                                fastSkipState?.stopSkipping(it)
                            }
                            ticket = null
                            seekJob = null
                        }
                    }
                }
                true
            } else if (event.type == KeyEventType.KeyUp) {
                val isSkipping = ticket != null
                seekJob?.cancel()
                seekJob = null
                if (!isSkipping) {
                    // 未进入快进, 视为短按 seek
                    onForwardState()
                }
                true
            } else {
                false
            }
        } else {
            false
        }
    }
}
