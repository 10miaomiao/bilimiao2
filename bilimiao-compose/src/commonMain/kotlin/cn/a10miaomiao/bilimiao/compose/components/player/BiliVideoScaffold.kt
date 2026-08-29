@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.runtime.collectAsState

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PictureInPicture
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.ORIENTATION_LANDSCAPE
import cn.a10miaomiao.bilimiao.compose.ORIENTATION_PORTRAIT
import cn.a10miaomiao.bilimiao.compose.common.HapticFeedbackType
import cn.a10miaomiao.bilimiao.compose.common.LocalPlayerState
import cn.a10miaomiao.bilimiao.compose.common.isCompactWindow
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigator
import cn.a10miaomiao.bilimiao.compose.common.rememberHapticFeedback
import cn.a10miaomiao.bilimiao.compose.components.layout.PlayerDisplayMode
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.FastForwardIndicator
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.VideoScaffold
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.GestureIndicatorState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.LockableVideoGestureHost
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.NoOpLevelController
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.rememberGestureIndicatorState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.rememberPlayerFastSkipState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.rememberSwipeSeekerState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.MediaProgressIndicatorText
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.MediaProgressSliderDefaults
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.PlayerControllerBar
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.PlayerControllerDefaults
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.PlayerProgressSliderState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.rememberVideoControllerState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.top.PlayerMoreActionsButton
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.top.PlayerTopBar
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.VideoLoadingIndicator
import cn.a10miaomiao.bilimiao.compose.pages.player.SendDanmakuPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.DanmakuDisplaySettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.VideoSettingPage
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.mapPreferences
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegateImpl
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlaybackStatus
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.SubtitleSourceInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.restorePlayerSystemBars
import com.a10miaomiao.bilimiao.comm.delegate.player.setPlayerFullscreenSystemBars
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import org.kodein.di.compose.rememberInstance
import org.openani.mediamp.MediampPlayer

/**
 * bilimiao 视频播放器容器
 *
 * 整合 animeko 迁移的 [VideoScaffold]、[PlayerControllerBar]、[LockableVideoGestureHost] 等，
 * 连接到 [PlayerDelegateImpl] 统一管理播放状态。
 *
 * 播放窗口模式与 [ComposeScaffoldPlayerLayoutState][cn.a10miaomiao.bilimiao.compose.components.layout.ComposeScaffoldPlayerLayoutState]
 * 同步, 通过 [LocalPlayerState] 读取 [PlayerDisplayMode].
 *
 * - 全屏模式 ([PlayerDisplayMode.Fullscreen]): 顶栏导航图标为 ArrowBack, 点击退出全屏而非关闭播放.
 * - 非全屏模式: 顶栏导航图标为 Close, 点击关闭播放.
 * - 悬浮横屏模式 ([PlayerDisplayMode.FloatingLandscape]): 关闭手势操作.
 * - 非全屏模式: 控制器布局忽略窗口安全边距.
 *
 * 替代旧的 `VideoScaffold.kt`（已被删除）。
 *
 * @param delegate 播放器代理
 * @param modifier 布局修饰符
 * @param onBack 关闭播放回调（非全屏时顶栏 Close 按钮）
 * @param onToggleFullscreen 全屏切换回调
 * @param onExitFullscreen 退出全屏回调（全屏时顶栏 ArrowBack 按钮），默认与 [onToggleFullscreen] 相同
 */
/**
 * 未登录时最高可选清晰度（480P）
 */
private const val MAX_QUALITY_NOT_LOGIN = 48

/**
 * 非大会员最高可选清晰度（1080P）
 */
private const val MAX_QUALITY_NOT_VIP = 80

/**
 * 长按画面快进倍速（2 倍速，松手恢复原倍速）
 */
private const val FAST_FORWARD_SPEED = 2.0f

@Composable
fun BiliVideoScaffold(
    delegate: BasePlayerDelegate,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onToggleFullscreen: () -> Unit = {},
    onExitFullscreen: () -> Unit = onToggleFullscreen,
) {
    val playerDelegate = delegate as PlayerDelegateImpl
    val player: MediampPlayer? = playerDelegate.mediampPlayer
    // 播放状态（低频，见 PlaybackState）与播放源状态（见 PlayerSourceState）分别订阅；
    // 当前播放位置高频更新，独立 StateFlow 订阅（by delegate 保证进度条闭包读取最新值）
    val playbackState by playerDelegate.playbackState.collectAsState()
    val sourceState by playerDelegate.sourceState.collectAsState()
    val currentPosition by playerDelegate.currentPosition.collectAsState()
    val status = playbackState.status
    val isPlaying = status == PlaybackStatus.Playing
    val isLoading = status == PlaybackStatus.Loading
    val isCompleted = status == PlaybackStatus.Completed
    val duration = playbackState.duration
    val loadingMessage = playbackState.loadingMessage
    val errorMessage = playbackState.errorMessage
    val danmakuVisible = playbackState.danmakuVisible
    val volume = playbackState.volume
    val playbackSpeed = playbackState.playbackSpeed
    val currentSource = sourceState.currentSource
    val playbackInfo = sourceState.playbackInfo
    val currentQuality = sourceState.currentQuality
    val danmakuParser = sourceState.danmakuParser
    val subtitleList = sourceState.subtitleList
    val currentSubtitle = sourceState.currentSubtitle

    // 播放器控制依赖的服务（通过 Kodein 注入）
    val userStore: UserStore by rememberInstance()
    val pageNavigator: PageNavigator by rememberInstance()

    // 倍速菜单预设值（设置中的 PlayerSpeedValues，默认 0.5x/1.0x/2.0x）
    var speedOptions by remember {
        mutableStateOf(SettingConstants.PLAYER_SPEED_SETS.map { it.toFloat() }.sorted())
    }
    LaunchedEffect(Unit) {
        speedOptions = SettingPreferences.mapPreferences {
            (it[SettingPreferences.PlayerSpeedValues] ?: SettingConstants.PLAYER_SPEED_SETS)
                .map { value -> value.toFloat() }
                .sorted()
        }
    }

    // 与 ComposeScaffoldPlayerLayoutState.displayMode 同步: 两者均基于同一 PlayerState 字段 + isCompactWindow() 推导
    val playerState = LocalPlayerState.current
    // 全屏状态统一由 PlayerState 提供（数据源为 FullscreenController.isFullscreen）
    val isFullscreen by playerState.fullScreenPlayer.collectAsState()
    // ComposeScaffold 中 orientation = if (isCompactWindow()) PORTRAIT else LANDSCAPE
    val scaffoldOrientation = if (isCompactWindow()) ORIENTATION_PORTRAIT else ORIENTATION_LANDSCAPE
    val displayMode = when {
        !playerState.showPlayer -> PlayerDisplayMode.Hidden
        isFullscreen -> PlayerDisplayMode.Fullscreen
        playerState.anchorBounds != null -> PlayerDisplayMode.AnchorOverlay
        scaffoldOrientation == ORIENTATION_PORTRAIT -> PlayerDisplayMode.EmbeddedPortrait
        scaffoldOrientation == ORIENTATION_LANDSCAPE -> PlayerDisplayMode.FloatingLandscape
        else -> PlayerDisplayMode.Hidden
    }
    // 悬浮横屏模式关闭手势操作
    val gesturesEnabled = displayMode != PlayerDisplayMode.FloatingLandscape
    val contentWindowInsets = if (isFullscreen) {
        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
    } else {
        WindowInsets(0.dp)
    }

    val controllerState = rememberVideoControllerState()
    var isLocked by remember { mutableStateOf(false) }
    val indicatorState = rememberGestureIndicatorState()

    // 全屏播放时控制系统栏（状态栏/导航条）显示：
    // - 控制器隐藏时，状态栏/导航栏均隐藏，实现沉浸式全屏
    // - 控制器激活显示时，仅显示状态栏（导航栏保持隐藏），且状态栏前景色为白色
    // - 退出全屏或组件卸载时，恢复系统栏
    val controllerVisibility = controllerState.visibility
    DisposableEffect(isFullscreen, isLocked, controllerVisibility) {
        val controllerActive = !isLocked &&
            (controllerVisibility.topBar || controllerVisibility.bottomBar)
        if (isFullscreen) {
            setPlayerFullscreenSystemBars(
                statusBarVisible = controllerActive,
                navigationBarVisible = false,
            )
        } else {
            restorePlayerSystemBars()
        }
        onDispose {
            restorePlayerSystemBars()
        }
    }

    player?.let { p ->
        // duration 为低频合并状态（普通 val），remember(player) 闭包会捕获创建时的值快照，
        // 用 rememberUpdatedState 保持闭包读取最新值（进度条总时长）
        val currentDuration by rememberUpdatedState(duration)
        val progressSliderState = remember(player) {
            PlayerProgressSliderState(
                currentPositionMillis = { currentPosition },
                totalDurationMillis = { currentDuration },
                chapters = { emptyList() },
                onPreview = { pos -> playerDelegate.seekTo(pos) },
                onPreviewFinished = { pos -> playerDelegate.seekTo(pos) },
            )
        }

        // 长按画面 2 倍速快进状态（手势宿主与悬浮提示共用）
        val hapticFeedback = rememberHapticFeedback()
        var speedBeforeFastSkip by remember { mutableFloatStateOf(playbackSpeed) }
        var fastForwarding by remember { mutableStateOf(false) }
        val fastSkipState = rememberPlayerFastSkipState(
            gestureIndicatorState = indicatorState,
            onStart = {
                speedBeforeFastSkip = playbackSpeed
                playerDelegate.setPlaybackSpeed(FAST_FORWARD_SPEED)
                fastForwarding = true
                // 长按触发震动反馈（对齐旧版 performHapticFeedback(LONG_PRESS)）
                hapticFeedback.perform(HapticFeedbackType.LONG_PRESS)
            },
            onStop = {
                playerDelegate.setPlaybackSpeed(speedBeforeFastSkip)
                fastForwarding = false
            },
            fastForwardSpeed = FAST_FORWARD_SPEED,
        )

        val enter = fadeIn()
        val exit = fadeOut()

        VideoScaffold(
            expanded = isFullscreen,
            modifier = modifier,
            controllerState = controllerState,
            gestureLocked = isLocked,
            contentWindowInsets = contentWindowInsets,
            topBar = {
                PlayerTopBar(
                    title = currentSource?.title ?: "",
                    isFullscreen = displayMode == PlayerDisplayMode.Fullscreen,
                    onExitFullscreen = onExitFullscreen,
                    onClose = onBack,
                    actions = {
                        // 画中画（仅安卓 8.0+ 支持，桌面端暂不支持）
                        if (isPictureInPictureSupported()) {
                            IconButton(
                                onClick = {
                                    val info = playbackInfo
                                    val width = info?.width ?: 16
                                    val height = info?.height ?: 9
                                    if (!enterPictureInPictureMode(width, height)) {
                                        GlobalToaster.show("此设备不支持小窗播放")
                                    }
                                },
                            ) {
                                Icon(
                                    Icons.Rounded.PictureInPicture,
                                    "画中画",
                                    tint = LocalContentColor.current,
                                )
                            }
                        }
                        PlayerMoreActionsButton(
                            onVideoSetting = {
                                pageNavigator.navigate(VideoSettingPage())
                            },
                            onDanmakuSetting = {
                                // 直接打开当前播放模式的弹幕显示设置（对齐原安卓版行为）
                                val modeName = if (isFullscreen) {
                                    SettingPreferences.DanmakuFullMode.name
                                } else {
                                    SettingPreferences.DanmakuSmallMode.name
                                }
                                pageNavigator.navigate(DanmakuDisplaySettingPage(modeName))
                            },
                        )
                    },
                )
            },
            video = {
                VideoPlayer(
                    player = p,
                    modifier = Modifier.fillMaxSize(),
                )
            },
            danmakuHost = {
                DanmakuOverlay(
                    currentPosition = currentPosition,
                    isPlaying = isPlaying,
                    danmakuParser = danmakuParser,
                    visible = danmakuVisible,
                    modifier = Modifier.fillMaxSize(),
                )
            },
            gestureHost = {
                // 悬浮横屏模式关闭手势操作
                if (gesturesEnabled) {
                    val swipeSeekerState = rememberSwipeSeekerState(
                        screenWidthPx = constraints.maxWidth,
                        // 最终 seek 由 SwipeSeekInteraction 经 finishPreview() 以
                        // "手势起点 + 偏移" 的绝对位置提交. 这里不能再用相对 skip:
                        // 拖动期间预览已经 seek 过播放器, 再相对 skip 会二次叠加偏移.
                        onSeek = { _ -> },
                    )
                    LockableVideoGestureHost(
                        controllerState = controllerState,
                        seekerState = swipeSeekerState,
                        progressSliderState = progressSliderState,
                        locked = isLocked,
                        enableSwipeToSeek = duration > 0,
                        audioController = NoOpLevelController,
                        brightnessController = NoOpLevelController,
                        playbackSpeedControllerState = null,
                        onTogglePauseResume = {
                            if (isPlaying) playerDelegate.pause() else playerDelegate.resume()
                        },
                        onToggleFullscreen = onToggleFullscreen,
                        onExitFullscreen = onExitFullscreen,
                        onToggleDanmaku = { playerDelegate.toggleDanmaku() },
                        gestureIndicatorState = indicatorState,
                        fastSkipState = fastSkipState,
                    )
                }
            },
            floatingMessage = {
                // 长按倍速浮窗（样式对齐旧版），居中偏上
                if (fastForwarding) {
                    Box(
                        Modifier.align(Alignment.TopCenter)
                            .padding(top = 40.dp)
                    ) {
                        FastForwardIndicator(
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
                if (isLoading) {
                    VideoLoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        showProgress = true,
                        text = { androidx.compose.material3.Text(loadingMessage) },
                    )
                }
                errorMessage?.let { msg ->
                    VideoLoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        showProgress = false,
                        text = { androidx.compose.material3.Text(msg) },
                    )
                }
                if (isCompleted) {
                    VideoLoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        showProgress = false,
                        text = { androidx.compose.material3.Text("播放完成") },
                    )
                }
            },
            bottomBar = {
                PlayerControllerBar(
                    startActions = {
                        PlayerControllerDefaults.PlaybackIcon(
                            isPlaying = { isPlaying },
                            onClick = {
                                if (isPlaying) playerDelegate.pause() else playerDelegate.resume()
                            },
                        )
                        PlayerControllerDefaults.DanmakuIcon(
                            danmakuEnabled = danmakuVisible,
                            onClick = { playerDelegate.toggleDanmaku() },
                        )
                    },
                    progressIndicator = {
                        MediaProgressIndicatorText(progressSliderState)
                    },
                    progressSlider = {
                        PlayerControllerDefaults.MediaProgressSlider(
                            progressSliderState = progressSliderState,
                            cacheProgressInfoFlow = kotlinx.coroutines.flow.flowOf(null),
                        )
                    },
                    danmakuEditor = {
                        PlayerControllerDefaults.DanmakuSendEntry(
                            onClick = {
                                if (!userStore.isLogin()) {
                                    GlobalToaster.show("请先登录")
                                } else {
                                    // 全屏播放时暂停并隐藏控制器，便于输入弹幕
                                    if (isFullscreen && isPlaying) {
                                        playerDelegate.pause()
                                    }
                                    pageNavigator.navigate(SendDanmakuPage())
                                }
                            },
                        )
                    },
                    endActions = {
                        if (isFullscreen) {
                            // 清晰度切换（需要登录/大会员的清晰度置灰）
                            PlayerControllerDefaults.QualitySwitcher(
                                currentQuality = currentQuality,
                                options = playbackInfo?.acceptList ?: emptyList(),
                                onValueChange = { quality -> playerDelegate.changeQuality(quality) },
                                isOptionEnabled = { accept ->
                                    when {
                                        // 1080P 以上需要大会员
                                        accept.quality > MAX_QUALITY_NOT_VIP -> userStore.isVip()
                                        // 480P 以上需要登录
                                        accept.quality > MAX_QUALITY_NOT_LOGIN -> userStore.isLogin()
                                        else -> true
                                    }
                                },
                            )
                            // 倍速菜单
                            PlayerControllerDefaults.SpeedSelector(
                                currentSpeed = playbackSpeed,
                                options = speedOptions,
                                onValueChange = { speed -> playerDelegate.setPlaybackSpeed(speed) },
                            )
                            // CC 字幕选择（无可用字幕时自动隐藏）
                            PlayerControllerDefaults.SubtitleSwitcher(
                                currentSubtitle = currentSubtitle,
                                options = listOf<SubtitleSourceInfo?>(null) + subtitleList,
                                onValueChange = { subtitle -> playerDelegate.setSubtitle(subtitle) },
                            )
                        }
                        // 全屏按钮
                        PlayerControllerDefaults.FullscreenIcon(
                            isFullscreen = isFullscreen,
                            onClickFullscreen = onToggleFullscreen,
                        )
                    },
                    expanded = isFullscreen,
                    sliderOnly = controllerState.visibility.let {
                        it.bottomBar && !it.topBar
                    },
                )
            },
            detachedProgressSlider = {
                PlayerControllerDefaults.MediaProgressSlider(
                    progressSliderState = progressSliderState,
                    cacheProgressInfoFlow = kotlinx.coroutines.flow.flowOf(null),
                )
            },
            floatingBottomEnd = {
//                PlayerControllerDefaults.FullscreenIcon(
//                    isFullscreen = isFullscreen,
//                    onClickFullscreen = onToggleFullscreen,
//                )
            },
            gestureLock = {
                cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.GestureLock(
                    isLocked = isLocked,
                    onClick = { isLocked = !isLocked },
                )
            },
        )
    }
}
