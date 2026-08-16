@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.systemGesturesPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.ControllerVisibility
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.GestureFamily
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.PlaybackSpeedControllerState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.PlayerControllerState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.PlayerProgressSliderState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.SwipeSeekerState.Companion.swipeToSeek

val VIDEO_GESTURE_MOUSE_MOVE_SHOW_CONTROLLER_DURATION = 3.seconds
val VIDEO_GESTURE_TOUCH_SHOW_CONTROLLER_DURATION = 3.seconds

/**
 * 条件性地应用 Modifier, 替代原 animeko 的 `ifThen`.
 *
 * 仅当 [condition] 为 `true` 时, 将 [content] 返回的 Modifier 作用于本 Modifier.
 */
@Composable
private fun Modifier.thenIf(
    condition: Boolean,
    content: @Composable Modifier.() -> Modifier,
): Modifier = if (condition) this.content() else this

/**
 * 将屏幕横滑 seek 的状态映射到控制器显隐和进度预览.
 *
 * [SwipeSeekerState] 负责识别手势, 本类只响应开始、取消状态变化和结束事件.
 */
private class SwipeSeekInteraction(
    private val controllerState: PlayerControllerState,
    private val seekerState: SwipeSeekerState,
    private val progressSliderState: PlayerProgressSliderState,
) {
    fun onStarted() {
        if (controllerState.visibility.bottomBar) {
            controllerState.setRequestInlineProgressSlider(this)
        } else {
            controllerState.setRequestProgressBar(this)
        }
    }

    fun onCancellationChanged(cancelled: Boolean) {
        if (cancelled) {
            progressSliderState.cancelPreview()
        } else {
            updatePreview()
        }
    }

    fun updatePreview() {
        if (seekerState.isCancelled) {
            progressSliderState.cancelPreview()
            return
        }
        if (progressSliderState.totalDurationMillis == 0L) return

        val previewPositionMillis =
            progressSliderState.currentPositionMillis + seekerState.deltaSeconds.times(1000)
        val offsetRatio = previewPositionMillis.toFloat() / progressSliderState.totalDurationMillis
        progressSliderState.previewPositionRatio(offsetRatio.coerceIn(0f, 1f))
    }

    fun onStopped(cancelled: Boolean) {
        cancelControllerRequest()
        if (cancelled) {
            progressSliderState.cancelPreview()
        } else {
            progressSliderState.finishPreview()
        }
    }

    fun dispose() {
        cancelControllerRequest()
    }

    private fun cancelControllerRequest() {
        controllerState.cancelRequestInlineProgressSlider(this)
        controllerState.cancelRequestProgressBarVisible(this)
    }
}

@Composable
private fun rememberSwipeSeekInteraction(
    controllerState: PlayerControllerState,
    seekerState: SwipeSeekerState,
    progressSliderState: PlayerProgressSliderState,
): SwipeSeekInteraction {
    val interaction = remember(controllerState, seekerState, progressSliderState) {
        SwipeSeekInteraction(controllerState, seekerState, progressSliderState)
    }
    DisposableEffect(interaction) {
        onDispose(interaction::dispose)
    }
    return interaction
}

/**
 * 播放器手势宿主, 处理所有播放相关手势.
 *
 * 手势行为由 [family] 决定, 触摸家族 (默认) 支持:
 * - 点击切换控制器显隐
 * - 双击暂停/恢复
 * - 横向滑动快进/快退 (seek)
 * - 左侧滑动调节亮度, 右侧滑动调节音量
 * - 中间垂直滑动进入/退出全屏
 * - 长按快进
 *
 * 鼠标家族支持点击暂停、双击全屏、滚轮调节音量、悬停显示控制器.
 */
@Composable
fun PlayerGestureHost(
    controllerState: PlayerControllerState,
    seekerState: SwipeSeekerState,
    progressSliderState: PlayerProgressSliderState,
    indicatorState: GestureIndicatorState,
    fastSkipState: FastSkipState?,
    enableSwipeToSeek: Boolean,
    audioController: LevelController,
    brightnessController: LevelController,
    playbackSpeedControllerState: PlaybackSpeedControllerState?,
    modifier: Modifier = Modifier,
    family: GestureFamily = GestureFamily.TOUCH,
    onTogglePauseResume: () -> Unit = {},
    onToggleFullscreen: () -> Unit = {},
    onExitFullscreen: () -> Unit = {},
    onToggleDanmaku: () -> Unit = {},
    onTogglePlayerStats: () -> Unit = {},
) {
    val onTogglePauseResumeState by rememberUpdatedState(onTogglePauseResume)

    BoxWithConstraints {
        Row(
            Modifier.align(Alignment.TopCenter)
                .systemGesturesPadding()
                .padding(top = 16.dp),
        ) {
            GestureIndicator(indicatorState, swipeSeekerState = seekerState)
        }
        val maxHeight = maxHeight
        val adjustingVolumeOrBrightness =
            indicatorState.visible && (indicatorState.state == GestureIndicatorState.State.VOLUME || indicatorState.state == GestureIndicatorState.State.BRIGHTNESS)
        val adjustingForwardOrBackward =
            indicatorState.visible && (indicatorState.state == GestureIndicatorState.State.FAST_FORWARD || indicatorState.state == GestureIndicatorState.State.FAST_BACKWARD)

        // 替代原 animeko 的 rememberUiMonoTasker: 使用 rememberCoroutineScope + launch
        val indicatorScope = rememberCoroutineScope()
        // 简化: 原始实现根据 family == MOUSE 决定是否使用 mediamp 的 AudioLevelController,
        // 此处统一使用 audioController (LevelController), 故 useMediaAudioController 恒为 false.
        val useMediaAudioController = false
        val systemFullscreen = false // 替代 isSystemInFullscreen()

        val keyboardModifier = modifier
            .testTag("VideoGestureHost")
            .playerKeyboardShortcuts(
                seekerState = seekerState,
                fastSkipState = fastSkipState,
                currentPlaybackSpeed = playbackSpeedControllerState?.currentSpeed,
                playbackSpeedRange = playbackSpeedControllerState?.speedRange
                    ?: PlaybackSpeedControllerState.DEFAULT_SPEED_RANGE,
                onPlaybackSpeedChanged = {
                    playbackSpeedControllerState?.commitSpeed(it)
                    indicatorScope.launch { indicatorState.showPlaybackSpeed(it) }
                },
                volumeEnabled = !useMediaAudioController,
                onVolumeUp = { fineAdjustment ->
                    audioController.increaseLevel(if (fineAdjustment) audioController.levelStep else 0.10f)
                    indicatorScope.launch {
                        indicatorState.showVolumeRange(audioController.level)
                    }
                },
                onVolumeDown = { fineAdjustment ->
                    audioController.decreaseLevel(if (fineAdjustment) audioController.levelStep else 0.10f)
                    indicatorScope.launch {
                        indicatorState.showVolumeRange(audioController.level)
                    }
                },
                onTogglePauseResume = onTogglePauseResumeState,
                onToggleFullscreen = onToggleFullscreen,
                onExitFullscreen = onExitFullscreen,
                onToggleDanmaku = onToggleDanmaku,
                onTogglePlayerStats = onTogglePlayerStats,
            )
        // 替代原 animeko 的 playerFocusHost: focus 管理简化为直接使用 keyboardModifier

        if (family.autoHideController) {
            LaunchedEffect(controllerState.visibility, controllerState.alwaysOn) {
                if (controllerState.alwaysOn) return@LaunchedEffect
                if (controllerState.visibility.bottomBar) {
                    delay(VIDEO_GESTURE_TOUCH_SHOW_CONTROLLER_DURATION)
                    controllerState.toggleFullVisible(false)
                }
            }
        }

        if (family.mouseHoverForController) {
            // 没有人请求 alwaysOn 时自动隐藏控制器
            LaunchedEffect(controllerState) {
                snapshotFlow { controllerState.alwaysOn }.collectLatest { alwaysOn ->
                    if (alwaysOn) return@collectLatest
                    snapshotFlow { controllerState.visibility != ControllerVisibility.Invisible }.collectLatest {
                        if (!it) {
                            delay(VIDEO_GESTURE_MOUSE_MOVE_SHOW_CONTROLLER_DURATION)
                            controllerState.toggleFullVisible(false)
                        }
                    }
                }
            }
        }

        @Composable
        fun Modifier.combineClickableWithFamilyGesture() = this then
                combinedClickable(
                    remember { MutableInteractionSource() },
                    indication = null,
                    onClick = remember(family) {
                        {
                            if (family.clickToPauseResume) {
                                onTogglePauseResumeState()
                            }
                            if (family.clickToToggleController) {
                                controllerState.toggleFullVisible()
                            }
                            // 简化: playerFocusState.requestPlayerFocus() 省略
                        }
                    },
                    onDoubleClick = remember(family, onToggleFullscreen) {
                        {
                            if (family.doubleClickToFullscreen) {
                                onToggleFullscreen()
                            }
                            if (family.doubleClickToPauseResume) {
                                onTogglePauseResumeState()
                            }
                            // 简化: playerFocusState.requestPlayerFocus() 省略
                        }
                    },
                )

        // 替代原 animeko 的 rememberUiMonoTasker (鼠标移动延迟隐藏)
        val mouseMoveScope = rememberCoroutineScope()
        Box(
            keyboardModifier
                .combineClickableWithFamilyGesture()
                .thenIf(family.swipeToSeek && enableSwipeToSeek) {
                    val swipeSeekInteraction = rememberSwipeSeekInteraction(
                        controllerState,
                        seekerState,
                        progressSliderState,
                    )
                    swipeToSeek(
                        seekerState,
                        Orientation.Horizontal,
                        // 调节音量/亮度时禁用水平 seek
                        enabled = !adjustingVolumeOrBrightness,
                        onDragStarted = {
                            swipeSeekInteraction.onStarted()
                        },
                        onDragStopped = { _, cancelled ->
                            swipeSeekInteraction.onStopped(cancelled)
                        },
                        onCancellationChanged = { cancelled ->
                            swipeSeekInteraction.onCancellationChanged(cancelled)
                        },
                    ) {
                        swipeSeekInteraction.updatePreview()
                    }
                }
                .thenIf(family.mouseHoverForController) {
                    // 这里不能用 hover, 因为当控制器隐藏后 hover 状态仍然存在,
                    // 下次移动鼠标时不会重复触发 hover 事件, 也就无法显示.
                    // See test case: `mouse - mouseHoverForController - center screen twice`
                    pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == PointerEventType.Move) {
                                    controllerState.toggleFullVisible(true)
                                    mouseMoveScope.launch {
                                        delay(VIDEO_GESTURE_MOUSE_MOVE_SHOW_CONTROLLER_DURATION)
                                        controllerState.toggleFullVisible(false)
                                    }
                                }
                            }
                        }
                    }
                }
                .thenIf(family.scrollForVolume) {
                    // 简化: 原始实现使用 mediamp AudioLevelController, 此处用 audioController
                    pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == PointerEventType.Scroll) {
                                    event.changes.firstOrNull()?.scrollDelta?.y?.run {
                                        if (this < 0) audioController.increaseLevel()
                                        else if (this > 0) audioController.decreaseLevel()

                                        indicatorScope.launch {
                                            indicatorState.showVolumeRange(audioController.level)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // 不要删除此 focusable, 它与 combinedClickable 并非冗余.
                // combinedClickable 的焦点目标在 Android 触摸模式下不可聚焦,
                // 这个始终可聚焦的子节点是保持硬件快捷键可用的回退.
                .focusable()
                .fillMaxSize(),
        ) {
            Row(
                Modifier.matchParentSize()
                    .thenIf(
                        family.swipeLhsForBrightness ||
                                family.swipeRhsForVolume ||
                                family.swipeMidForFullscreen ||
                                family.longPressForFastSkip,
                    ) {
                        systemGesturesPadding()
                    }
                    .thenIf(family.longPressForFastSkip) {
                        fastSkipState?.let {
                            longPressFastSkip(it, SkipDirection.FORWARD)
                        } ?: Modifier
                    },
            ) {
                Box(
                    Modifier
                        .thenIf(family.swipeLhsForBrightness) {
                            swipeLevelControlWithIndicator(
                                brightnessController,
                                ((maxHeight - 100.dp) / 40).coerceAtLeast(2.dp),
                                Orientation.Vertical,
                                indicatorState,
                                enabled = !seekerState.isSeeking && !adjustingForwardOrBackward,
                                step = 0.01f,
                                setup = {
                                    indicatorState.state = GestureIndicatorState.State.BRIGHTNESS
                                },
                            )
                        }
                        .weight(1f)
                        .fillMaxHeight(),
                )

                Box(
                    Modifier
                        .thenIf(family.swipeMidForFullscreen) {
                            swipeToFullscreen(
                                enabled = !seekerState.isSeeking && !adjustingVolumeOrBrightness && !adjustingForwardOrBackward,
                                onEnterFullscreen = {
                                    if (!systemFullscreen) onToggleFullscreen()
                                },
                                onExitFullscreen = {
                                    if (systemFullscreen) onExitFullscreen()
                                },
                            )
                        }
                        .weight(1f)
                        .fillMaxHeight(),
                )

                Box(
                    Modifier
                        .thenIf(family.swipeRhsForVolume) {
                            swipeLevelControlWithIndicator(
                                audioController,
                                ((maxHeight - 100.dp) / 40).coerceAtLeast(2.dp),
                                Orientation.Vertical,
                                indicatorState,
                                enabled = !seekerState.isSeeking && !adjustingForwardOrBackward,
                                step = 0.05f,
                                setup = {
                                    indicatorState.state = GestureIndicatorState.State.VOLUME
                                },
                            )
                        }
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }

        if (family.clickToToggleController && systemFullscreen) {
            // 状态栏区域响应点击手势
            Box(
                Modifier.fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.systemGestures)
                    .combineClickableWithFamilyGesture(),
            )
        }
    }
}
