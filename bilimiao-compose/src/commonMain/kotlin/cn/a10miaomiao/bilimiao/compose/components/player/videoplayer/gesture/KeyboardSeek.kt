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
import androidx.compose.ui.input.key.isShiftPressed
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

/** 键盘左右方向键短按 seek 的默认秒数. */
internal const val KEYBOARD_SEEK_SECONDS: Int = 5

/** 按住 Shift 时键盘左右方向键 seek 的秒数, 同时用于 PageUp / PageDown. */
internal const val KEYBOARD_SEEK_LARGE_SECONDS: Int = 30

/** 按住方向键多久后进入快进. */
private const val KEYBOARD_FAST_FORWARD_DELAY_MILLIS: Long = 200L

/**
 * 键盘左右方向键本次 seek 的秒数.
 *
 * @param shiftPressed 是否按住了 Shift 键
 */
private fun keyboardSeekSeconds(shiftPressed: Boolean): Int =
    if (shiftPressed) KEYBOARD_SEEK_LARGE_SECONDS else KEYBOARD_SEEK_SECONDS

/**
 * 键盘 seek 与长按快进.
 *
 * - 短按左右方向键: 触发 [onSeekBackward] / [onSeekForward], 参数为本次 seek 的秒数 (均为正数, 方向由回调区分)
 * - 长按右方向键 (200ms 后): 触发 [fastSkipState] 快进; 松手时若已进入快进则停止, 否则视为短按 seek
 * - [isEnabled] 返回 `false` 时不消费方向键, 把按键让给焦点系统或进度条;
 *   但按住期间才变为失能的情况仍必须走完松手清理, 否则已启动的快进任务会残留 (见下方事件处理)
 * - KeyUp 只与「本节点确实消费过的那次 KeyDown」成对处理: 孤立的 KeyUp (KeyDown 时失能, 或事件
 *   根本没到本节点) 一律原样放行, 既不会凭空 seek 一次, 也不会抢走下层的 KeyUp
 *   (material3 Slider 依赖 KeyUp 触发 onValueChangeFinished)
 *
 * 与 [onKeyboardHorizontalDirection] 不同, 这里按「时间方向」而不是「布局方向」解释左右键:
 * 视频进度没有随文字方向翻转的语义, RTL 布局下按右方向键同样应当是前进.
 *
 * 替代原 animeko 的 `rememberUiMonoTasker` 为 `rememberCoroutineScope` + 单任务 [Job] 管理.
 */
fun Modifier.keyboardSeekAndFastForward(
    onSeekBackward: (seconds: Int) -> Unit,
    onSeekForward: (seconds: Int) -> Unit,
    fastSkipState: FastSkipState?,
    isEnabled: () -> Boolean = { true },
): Modifier = composed {
    val onBackwardState by rememberUpdatedState(onSeekBackward)
    val onForwardState by rememberUpdatedState(onSeekForward)
    // 快进状态与可用性判断在长按协程 / 后续重组中都会用到, 统一读最新值
    val fastSkipStateState by rememberUpdatedState(fastSkipState)
    val enabledState by rememberUpdatedState(isEnabled)
    val scope = rememberCoroutineScope()
    var seekJob by remember { mutableStateOf<Job?>(null) }
    var ticket by remember { mutableStateOf<Int?>(null) }
    // 记录「本节点确实消费了本次 ←/→ 的 KeyDown」: KeyUp 只有与它成对时才处理,
    // 否则既可能出现孤立的 KeyUp seek, 也可能把 KeyUp 泄漏给下层
    // (例如 material3 Slider 依赖 KeyUp 触发 onValueChangeFinished).
    // 左右各用一个标记: 同时按住两个方向键时, 任一方向的 KeyUp 都不能顶替另一方向的配对状态
    var pressedLeftByUs by remember { mutableStateOf(false) }
    var pressedRightByUs by remember { mutableStateOf(false) }

    onPreviewKeyEvent { event ->
        val enabled = enabledState()
        // Shift 在此处读取: 长按加速期间松开 Shift 不应改变本次 seek 的秒数语义,
        // 秒数只在松手 (KeyUp) 那一刻参与计算
        val seconds = keyboardSeekSeconds(event.isShiftPressed)

        // 松手清理必须放在 enabled 早退之前: 若按住 → 期间 isEnabled 变为 false (焦点 / 拖动状态变化),
        // 早退会让 seekJob 永不取消、awaitCancellation() 一直挂着, 播放器永久停在快进倍速.
        if (event.type == KeyEventType.KeyUp && event.key == Key.DirectionRight) {
            // 本次 KeyUp 只与「本节点消费过的 KeyDown」配对处理:
            // KeyDown 时失能或事件根本没到本节点时属于孤儿 KeyUp, 不 seek 也不吞掉
            if (!pressedRightByUs) return@onPreviewKeyEvent false
            pressedRightByUs = false
            val isSkipping = ticket != null
            seekJob?.cancel()
            seekJob = null
            // 已进入快进: 松手只负责停止快进, 不再触发短按 seek;
            // 未进入快进且仍启用: 按原有语义视为短按 seek (失能则不 seek)
            if (!isSkipping && enabled) {
                onForwardState(seconds)
            }
            // KeyDown 已被本节点消费, KeyUp 必须成对消费, 不论此刻是否仍然启用
            return@onPreviewKeyEvent true
        }
        if (event.type == KeyEventType.KeyUp && event.key == Key.DirectionLeft) {
            // 与右方向键对称: 没有消费过 KeyDown 的 KeyUp 原样放行, 不留任何状态
            if (!pressedLeftByUs) return@onPreviewKeyEvent false
            pressedLeftByUs = false
            // 消费过 KeyDown 就成对消费 KeyUp; 但按住期间才失能时不做 seek
            if (enabled) {
                onBackwardState(seconds)
            }
            return@onPreviewKeyEvent true
        }

        if (!enabled) return@onPreviewKeyEvent false
        when (event.key) {
            Key.DirectionLeft -> when (event.type) {
                // 消费 KeyDown, 等待 KeyUp 再 seek
                KeyEventType.KeyDown -> {
                    pressedLeftByUs = true
                    true
                }

                else -> false
            }

            Key.DirectionRight -> when (event.type) {
                KeyEventType.KeyDown -> {
                    pressedRightByUs = true
                    // 若没有正在进行的快进任务, 启动一个: 200ms 后进入快进
                    if (seekJob == null) {
                        seekJob = scope.launch {
                            val thisJob = coroutineContext[Job]
                            // 本次任务自己写下的票据, 用于 finally 判断共享字段是否仍属于自己
                            var myTicket: Int? = null
                            try {
                                delay(KEYBOARD_FAST_FORWARD_DELAY_MILLIS)
                                fastSkipStateState?.startSkipping(SkipDirection.FORWARD)?.let {
                                    myTicket = it
                                    ticket = it
                                }
                                awaitCancellation()
                            } finally {
                                // stopSkipping 自带票据匹配 (过期的票据本身就是空操作), 因此无条件调用是安全的
                                myTicket?.let { ownedTicket ->
                                    fastSkipStateState?.stopSkipping(ownedTicket)
                                }
                                // 只在共享字段仍指向本次任务时才清空: 旧任务的取消续体可能在新一轮
                                // KeyDown 之后才被调度, 此时无条件清空会误删新任务的 job / 票据,
                                // 导致两个活动任务, 且新任务启动的快进再也不会被 stopSkipping 停止
                                if (ticket == myTicket) {
                                    ticket = null
                                }
                                if (seekJob === thisJob) {
                                    seekJob = null
                                }
                            }
                        }
                    }
                    true
                }

                else -> false
            }

            else -> false
        }
    }
}
