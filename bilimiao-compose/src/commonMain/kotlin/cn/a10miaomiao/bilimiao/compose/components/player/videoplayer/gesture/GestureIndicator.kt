@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.annotation.UiThread
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.BrightnessLow
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.GestureIndicatorState.State.BRIGHTNESS
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.GestureIndicatorState.State.FAST_BACKWARD
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.GestureIndicatorState.State.FAST_FORWARD
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.GestureIndicatorState.State.PAUSED_ONCE
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.GestureIndicatorState.State.PLAYBACK_SPEED
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.GestureIndicatorState.State.RESUMED_ONCE
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.GestureIndicatorState.State.SEEKING
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.GestureIndicatorState.State.VOLUME

@Stable
private fun renderTime(seconds: Int): String {
    // 将秒数格式化为 MM:SS, 两位补零. 替代原 animeko 的 fixToString.
    val minutes = (seconds / 60).toString().padStart(2, '0')
    val secs = (seconds % 60).toString().padStart(2, '0')
    return "$minutes:$secs"
}

@Composable
fun rememberGestureIndicatorState(): GestureIndicatorState = remember { GestureIndicatorState() }

@Stable
class GestureIndicatorState {
    internal enum class State {
        PAUSED_ONCE,
        RESUMED_ONCE,
        VOLUME,
        BRIGHTNESS,
        SEEKING,
        FAST_FORWARD,
        FAST_BACKWARD,
        PLAYBACK_SPEED,
    }

    internal var visible: Boolean by mutableStateOf(false)
    internal var state: State? by mutableStateOf(null)
    internal var progressValue: Float by mutableFloatStateOf(0f)
    internal var deltaSeconds: Int by mutableIntStateOf(0)
    internal var seekCancelled: Boolean by mutableStateOf(false)
    internal var playbackSpeed: Float by mutableFloatStateOf(1f)
    private var counter: Int = 0

    private inline fun startShow(
        state: State,
        setup: () -> Unit = {},
    ): Int {
        val ticket = ++counter
        setup()
        this.state = state
        visible = true
        return ticket
    }

    private inline fun show(
        state: State,
        setup: () -> Unit = {},
        action: () -> Unit
    ) {
        val ticket = ++counter
        try {
            setup()
            this.state = state
            visible = true
            action()
        } finally {
            if (this.counter == ticket && // 之后没有人再修改状态
                this.state == state
            ) {
                visible = false
            }
        }
    }

    private companion object {
        private const val LONG: Long = 700
        private const val SHORT: Long = 500
    }

    @UiThread
    suspend fun showPausedLong() {
        show(PAUSED_ONCE) {
            delay(LONG)
        }
    }

    @UiThread
    suspend fun showResumedLong() {
        show(RESUMED_ONCE) {
            delay(LONG)
        }
    }

    @UiThread
    suspend fun showVolumeRange(currentRatio: Float) {
        show(VOLUME, setup = { progressValue = currentRatio }) {
            delay(SHORT)
        }
    }

    @UiThread
    suspend fun showBrightnessRange(currentRatio: Float) {
        show(BRIGHTNESS, setup = { progressValue = currentRatio }) {
            delay(SHORT)
        }
    }

    @UiThread
    suspend fun showPlaybackSpeed(speed: Float) {
        show(PLAYBACK_SPEED, setup = { playbackSpeed = speed }) {
            delay(SHORT)
        }
    }

    @UiThread
    suspend fun showSeeking(
        deltaSeconds: Int,
    ) {
        show(SEEKING, setup = {
            this.deltaSeconds = deltaSeconds
            seekCancelled = false
        }) {
            delay(SHORT)
        }
    }

    @UiThread
    fun startSeekCancellation(): Int {
        return startShow(SEEKING) {
            seekCancelled = true
        }
    }

    @UiThread
    fun stopSeekCancellation(ticket: Int) {
        stopShow(ticket)
    }

    @UiThread
    fun startFastForward(): Int {
        startShow(FAST_FORWARD, setup = { })
        return counter
    }

    @UiThread
    fun stopFastForward(ticket: Int) {
        stopShow(ticket)
    }

    @UiThread
    fun startFastBackward(): Int {
        startShow(FAST_BACKWARD, setup = { })
        return counter
    }

    @UiThread
    fun stopFastBackward(ticket: Int) {
        stopShow(ticket)
    }

    private fun stopShow(ticket: Int) {
        if (ticket == this.counter) {
            visible = false
        }
    }
}

@Immutable
internal data class GestureIndicatorPresentation(
    val state: GestureIndicatorState.State,
    val deltaSeconds: Int,
    val seekCancelled: Boolean,
)

internal fun gestureIndicatorPresentation(
    state: GestureIndicatorState,
    activeSwipeSeekerState: SwipeSeekerState?,
): GestureIndicatorPresentation? {
    if (!state.visible && activeSwipeSeekerState == null) return null
    val presentationState = if (activeSwipeSeekerState != null) GestureIndicatorState.State.SEEKING
    else state.state ?: return null
    return GestureIndicatorPresentation(
        state = presentationState,
        deltaSeconds = activeSwipeSeekerState?.deltaSeconds ?: state.deltaSeconds,
        seekCancelled = activeSwipeSeekerState?.isCancelled ?: state.seekCancelled,
    )
}

/**
 * 格式化播放速度, 替代原 animeko 的 formatSpeedValue.
 * 保留一位小数, 例如 1.0, 1.5, 2.0.
 */
private fun formatSpeedValue(speed: Float): String {
    // 保留一位小数, 例如 1.0, 1.5, 2.0. 纯 Kotlin 实现以兼容 KMP commonMain.
    val rounded = (speed * 10).toInt() / 10f
    val intPart = rounded.toInt()
    val decPart = ((rounded - intPart) * 10).toInt()
    return "$intPart.$decPart"
}

/**
 * 展示当前快进/快退秒数的指示器.
 *
 * `<< 00:00` / `>> 00:00`
 */
@Composable
fun GestureIndicator(
    state: GestureIndicatorState,
    swipeSeekerState: SwipeSeekerState? = null,
) {
    val shape = MaterialTheme.shapes.small
    val colors = MaterialTheme.colorScheme
    val activeSwipeSeekerState = swipeSeekerState?.takeIf { it.isSeeking }
    val presentation = gestureIndicatorPresentation(state, activeSwipeSeekerState)
    // 淡出期间 presentation 为 null。滑动 seek 的指示器只由 swipeSeekerState 驱动,
    // GestureIndicatorState.state 全程为 null；不保留最后一帧的话，松手后会淡出一个空 Surface。
    // 在组合结束后才写入, 避免组合被丢弃时留下脏值; 淡出期间读到的是上一帧提交的快照。
    val retainedPresentation = remember { mutableStateOf<GestureIndicatorPresentation?>(null) }
    if (presentation != null) {
        SideEffect { retainedPresentation.value = presentation }
    }
    // presentation 非 null 时不读 retainedPresentation, 因此快进过程中不会因保留帧写入而多一次重组。
    val currentPresentation = presentation ?: retainedPresentation.value

    AnimatedVisibility(
        visible = presentation != null,
        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
        exit = fadeOut(tween(durationMillis = 500)),
    ) {
        currentPresentation ?: return@AnimatedVisibility
        Surface(
            Modifier.alpha(0.8f),
            color = colors.surface,
            shape = shape,
            shadowElevation = 1.dp,
            contentColor = colors.onSurface,
        ) {
            val iconSize = 36.dp
            ProvideTextStyle(MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)) {
                Row(
                    Modifier.background(Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .height(iconSize),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // 用于音量和亮度
                    val progressIndicator: @Composable () -> Unit = remember(state, colors) {
                        // 这个 remember 是必要的, 因为 Compose 不会记住 lambda,
                        // 在这个频繁变化的 composable 中会导致性能问题.
                        {
                            LinearProgressIndicator(
                                progress = { state.progressValue },
                                modifier = Modifier.width(80.dp),
                                color = colors.primary,
                                trackColor = colors.onSurface.copy(alpha = 0.5f),
                                drawStopIndicator = {},
                            )
                        }
                    }

                    when (currentPresentation.state) {
                        GestureIndicatorState.State.RESUMED_ONCE -> {
                            Icon(
                                Icons.Rounded.PlayArrow, null,
                                Modifier.size(iconSize).background(Color.Transparent),
                            )
                        }

                        GestureIndicatorState.State.PAUSED_ONCE -> {
                            Icon(Icons.Rounded.Pause, null, Modifier.size(iconSize))
                        }

                        GestureIndicatorState.State.SEEKING -> {
                            Icon(
                                when {
                                    currentPresentation.seekCancelled -> Icons.Rounded.Close
                                    currentPresentation.deltaSeconds > 0 -> Icons.Rounded.FastForward
                                    else -> Icons.Rounded.FastRewind
                                },
                                contentDescription = null,
                                modifier = Modifier.size(iconSize),
                            )
                            Text(
                                text = if (currentPresentation.seekCancelled) {
                                    "松开以取消"
                                } else {
                                    renderTime(currentPresentation.deltaSeconds.absoluteValue)
                                },
                                maxLines = 1,
                            )
                        }

                        GestureIndicatorState.State.VOLUME -> {
                            Icon(
                                Icons.AutoMirrored.Rounded.VolumeUp, null,
                                Modifier.size(iconSize),
                            )
                            progressIndicator()
                        }

                        GestureIndicatorState.State.BRIGHTNESS -> {
                            Icon(
                                when (state.progressValue) {
                                    in 0.67..1.0 -> Icons.Rounded.BrightnessHigh
                                    in 0.33..0.67 -> Icons.Rounded.BrightnessMedium
                                    else -> Icons.Rounded.BrightnessLow
                                },
                                null,
                                Modifier.size(iconSize),
                            )
                            progressIndicator()
                        }

                        GestureIndicatorState.State.FAST_FORWARD -> {
                            Icon(Icons.Rounded.FastForward, null, Modifier.size(iconSize))
                        }

                        GestureIndicatorState.State.FAST_BACKWARD -> {
                            Icon(Icons.Rounded.FastRewind, null, Modifier.size(iconSize))
                        }

                        GestureIndicatorState.State.PLAYBACK_SPEED -> {
                            Icon(Icons.Rounded.FastForward, null, Modifier.size(iconSize))
                            Text("${formatSpeedValue(state.playbackSpeed)}x", maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
