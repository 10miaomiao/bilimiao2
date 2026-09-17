@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.runtime.collectAsState

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PictureInPicture
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.ORIENTATION_LANDSCAPE
import cn.a10miaomiao.bilimiao.compose.ORIENTATION_PORTRAIT
import cn.a10miaomiao.bilimiao.compose.base.BottomSheetState
import cn.a10miaomiao.bilimiao.compose.common.HapticFeedbackType
import cn.a10miaomiao.bilimiao.compose.common.LocalPlayerState
import cn.a10miaomiao.bilimiao.compose.common.isCompactWindow
import cn.a10miaomiao.bilimiao.compose.common.rememberHapticFeedback
import cn.a10miaomiao.bilimiao.compose.components.layout.PlayerDisplayMode
import cn.a10miaomiao.bilimiao.compose.components.layout.calculatePlayerDisplayMode
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.FastForwardIndicator
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.VideoScaffold
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.GestureIndicatorState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.LevelController
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.LockableVideoGestureHost
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.rememberBrightnessLevelController
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
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.top.PlayerScreenType
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.top.PlayerScreenTypeButton
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.top.PlayerTopBar
import cn.a10miaomiao.bilimiao.compose.components.status.BiliAnimTV
import cn.a10miaomiao.bilimiao.compose.pages.player.SendDanmakuPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.DanmakuDisplaySettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.VideoSettingPage
import cn.a10miaomiao.bilimiao.compose.platform.LocalSystemBarsController
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.editPreferences
import com.a10miaomiao.bilimiao.comm.datastore.mapPreferences
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegateImpl
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlaybackStatus
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.SubtitleSourceInfo
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.features.VideoAspectRatio
import kotlin.math.roundToInt

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
 * - 悬浮横屏模式 ([PlayerDisplayMode.FloatingLandscape]): 完整手势关闭，仅保留单击切换控制器显隐.
 * - 画中画模式 ([PlayerDisplayMode.PictureInPicture]): 画面铺满整个小窗, 顶栏/底栏/侧边按钮等
 *   控制器全部隐藏, 弹幕按「画中画」弹幕显示设置渲染, 播放/暂停经系统动作按钮
 *   (见 [PictureInPicturePlaybackAction]) 操作; 状态由 PlayerState.pictureInPicture
 *   (平台层注入, 与 ComposeScaffold 共享) 驱动.
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

/**
 * 小屏（非全屏）模式画面左侧弹幕发送按钮的测试标签
 */
private const val TAG_SMALL_SCREEN_DANMAKU_SEND_BUTTON = "SmallScreenDanmakuSendButton"

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
    val isBuffering = status == PlaybackStatus.Buffering
    val isCompleted = status == PlaybackStatus.Completed
    val duration = playbackState.duration
    val errorMessage = playbackState.errorMessage
    // 播放异常 / 播放完成覆盖层：异常优先于完成展示
    val showErrorOverlay = status == PlaybackStatus.Error
    val showCompletionOverlay = isCompleted && !showErrorOverlay
    val showStatusOverlay = showErrorOverlay || showCompletionOverlay
    val danmakuVisible = playbackState.danmakuVisible
    val playbackSpeed = playbackState.playbackSpeed
    val currentSource = sourceState.currentSource
    val playbackInfo = sourceState.playbackInfo
    val currentQuality = sourceState.currentQuality
    val danmakuParser = sourceState.danmakuParser
    val subtitleList = sourceState.subtitleList
    val currentSubtitle = sourceState.currentSubtitle
    val subtitleItems = sourceState.subtitleItems

    // 播放器控制依赖的服务（通过 Kodein 注入）
    val userStore: UserStore by rememberInstance()
    val bottomSheetState: BottomSheetState by rememberInstance()

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

    // 画面比例（对齐旧版「画面比例」菜单，选择结果持久化在 PlayerScreenType 偏好项）
    val scope = rememberCoroutineScope()
    var screenType by remember { mutableStateOf(PlayerScreenType.Default) }
    LaunchedEffect(Unit) {
        // mapPreferences 的接收者是 SettingPreferences 对象，其中的 PlayerScreenType 偏好键
        // 会遮蔽同名枚举，因此枚举转换放在闭包外进行
        val savedScreenType = SettingPreferences.mapPreferences {
            it[SettingPreferences.PlayerScreenType]
        }
        screenType = PlayerScreenType.ofValue(savedScreenType ?: PlayerScreenType.Default.value)
    }

    // 与 ComposeScaffold/PlayerLayer 共用 calculatePlayerDisplayMode 统一判定显示模式
    val playerState = LocalPlayerState.current
    // 全屏状态统一由 PlayerState 提供（数据源为 FullscreenController.isFullscreen）
    val isFullscreen by playerState.fullScreenPlayer.collectAsState()
    // 画中画（应用外小窗）状态：由平台层（Activity 的 onPictureInPictureModeChanged）注入，
    // 与 ComposeScaffold 共享同一状态源：宿主布局让出整个窗口，本组件负责小窗内的播放器展示
    val pictureInPicture by playerState.pictureInPicture.collectAsState()
    // ComposeScaffold 中 orientation = if (isCompactWindow()) PORTRAIT else LANDSCAPE
    val scaffoldOrientation = if (isCompactWindow()) ORIENTATION_PORTRAIT else ORIENTATION_LANDSCAPE
    val displayMode = calculatePlayerDisplayMode(
        showPlayer = playerState.showPlayer,
        fullScreenPlayer = isFullscreen,
        pictureInPicture = pictureInPicture,
        anchorBounds = playerState.anchorBounds,
        orientation = scaffoldOrientation,
    )
    // 当前生效的画中画展示（未展示播放器时本组件不会被组合，与状态源等价）
    val inPictureInPicture = displayMode == PlayerDisplayMode.PictureInPicture
    // 画面铺满的布局条件：全屏或画中画窗口（画中画窗口内不做 16:9 限制，铺满整个小窗）
    val expandedLayout = isFullscreen || inPictureInPicture
    // 弹幕按当前展示方式读取对应设置（画中画使用独立的「画中画」弹幕显示设置）
    val danmakuModeName = when (displayMode) {
        PlayerDisplayMode.PictureInPicture -> SettingPreferences.DanmakuPipMode.name
        PlayerDisplayMode.Fullscreen -> SettingPreferences.DanmakuFullMode.name
        else -> SettingPreferences.DanmakuSmallMode.name
    }
    // 悬浮横屏模式关闭完整手势操作（拖动/缩放由外层悬浮窗口处理），仅保留单击切换控制器；
    // 画中画窗口内控制器恒隐藏，任何播放器手势都无意义，同样关闭
    val gesturesEnabled = displayMode != PlayerDisplayMode.FloatingLandscape &&
        !inPictureInPicture
    val contentWindowInsets = if (isFullscreen) {
        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
    } else {
        WindowInsets(0.dp)
    }

    val controllerState = rememberVideoControllerState()
    var isLocked by remember { mutableStateOf(false) }
    val indicatorState = rememberGestureIndicatorState()

    // 全屏播放时控制系统栏（状态栏/导航条）的显示与隐藏：
    // - 控制器隐藏时，状态栏/导航栏均隐藏，实现沉浸式全屏
    // - 控制器激活显示时，仅显示状态栏（导航栏保持隐藏）
    // - 退出全屏或组件卸载时，恢复系统栏
    // 系统栏显隐与状态栏前景色统一经 LocalSystemBarsController（SystemBarsController）
    // 操作同一平台实现；前景色由 ComposeScaffold 依据播放器模式/深色主题控制，此处不触碰。
    val systemBarsController = LocalSystemBarsController.current
    val controllerVisibility = controllerState.visibility
    DisposableEffect(isFullscreen, isLocked, controllerVisibility) {
        val controllerActive = !isLocked &&
            (controllerVisibility.topBar || controllerVisibility.bottomBar)
        if (isFullscreen) {
            systemBarsController.setSystemBarsVisible(
                statusBarVisible = controllerActive,
                navigationBarVisible = false,
            )
        } else {
            systemBarsController.restoreSystemBars()
        }
        onDispose {
            systemBarsController.restoreSystemBars()
        }
    }

    // 画中画动作按钮（播放/暂停）：画中画窗口内的画面不接收点击，播放控制只能交给系统绘制。
    // 进入画中画时注册动作按钮点击处理，播放状态变化时同步按钮图标，退出画中画时注销。
    PictureInPicturePlaybackAction(
        enabled = inPictureInPicture,
        isPlaying = isPlaying,
        onTogglePlayPause = {
            if (isPlaying) playerDelegate.pause() else playerDelegate.resume()
        },
    )

    player?.let { p ->
        // 画面比例：16:9 / 4:3 的画面框尺寸由 video 层布局限制，
        // 其余模式交由播放器自身的缩放模式决定（适应 / 裁减 / 拉伸）
        LaunchedEffect(p, screenType) {
            p.features[VideoAspectRatio]?.setMode(screenType.aspectRatioMode)
        }

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

        // 音量/亮度手势控制器:
        // - 音量 = 播放器自身音量 (PlayerDelegate.setVolume, 安卓/桌面共用 0-100 的音量状态)
        // - 亮度 = 平台窗口亮度 (安卓生效; 桌面不支持逐窗口亮度, 为 no-op)
        val audioController = remember(playerDelegate) { PlayerVolumeLevelController(playerDelegate) }
        val brightnessController = rememberBrightnessLevelController()

        VideoScaffold(
            expanded = expandedLayout,
            modifier = modifier,
            controllerState = controllerState,
            // 画中画窗口内控制器全部隐藏（手势层同步关闭），仅显示画面/弹幕/字幕；
            // 播放异常/播放完成覆盖层显示时同样关闭手势，避免手势穿透到覆盖层下方
            gestureLocked = isLocked || inPictureInPicture || showStatusOverlay,
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
                                    // 初始动作按钮按当前播放状态给出「暂停」或「播放」
                                    if (!enterPictureInPictureMode(width, height, isPlaying)) {
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
                        // 画面比例（默认比例 / 16:9 / 4:3 / 全屏裁减 / 全屏拉伸）
                        PlayerScreenTypeButton(
                            screenType = screenType,
                            onValueChange = { type ->
                                screenType = type
                                scope.launch {
                                    SettingPreferences.editPreferences {
                                        it[SettingPreferences.PlayerScreenType] = type.value
                                    }
                                }
                            },
                        )
                        PlayerMoreActionsButton(
                            onVideoSetting = {
                                // 以 bottom sheet 弹出播放设置（对齐旧版行为）
                                bottomSheetState.open(VideoSettingPage())
                            },
                            onDanmakuSetting = {
                                // 以 bottom sheet 弹出当前播放模式的弹幕显示设置（对齐旧版行为）
                                bottomSheetState.open(DanmakuDisplaySettingPage(danmakuModeName))
                            },
                        )
                    },
                )
            },
            video = {
                // 16:9 / 4:3 时把画面框按目标比例居中（对齐旧版 GSY 的测量逻辑：
                // 画面框缩到目标比例，画面拉伸填满该框）；其余模式填满整个画面区域，
                // 由播放器的缩放模式决定画面是适应、裁减还是拉伸。
                val frameAspectRatio = screenType.frameAspectRatio
                VideoPlayer(
                    player = p,
                    modifier = if (frameAspectRatio == null) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier.align(Alignment.Center).aspectRatio(frameAspectRatio)
                    },
                )
            },
            danmakuHost = {
                DanmakuOverlay(
                    currentPosition = currentPosition,
                    isPlaying = isPlaying,
                    danmakuParser = danmakuParser,
                    // 发送成功的弹幕本地回显（带边框区分其它弹幕）
                    localDanmakuFlow = playerDelegate.localDanmakuFlow,
                    // 按当前播放模式读取对应的弹幕显示设置
                    modeName = danmakuModeName,
                    visible = danmakuVisible,
                    modifier = Modifier.fillMaxSize(),
                )
            },
            // CC 字幕层：位于视频底部、控制栏上方，按播放位置绘制当前字幕
            subtitle = {
                SubtitleOverlay(
                    subtitleItems = subtitleItems,
                    currentPosition = currentPosition,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            gestureHost = {
                // 悬浮横屏模式关闭完整手势操作（拖动/缩放由外层悬浮窗口处理），
                // 但仍保留“单击切换控制器显隐”：外层拖动手势只消费位移超过阈值的事件，
                // 纯单击不拦截，事件能到达这里。
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
                        audioController = audioController,
                        brightnessController = brightnessController,
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
                } else {
                    // 单击切换控制器显隐；detectTapGestures 超过触摸滑动阈值或事件被
                    // 外层拖动消费后会自动取消，不影响窗口拖动/缩放手势。
                    // 画中画窗口内控制器不参与展示（见 gestureLocked），此层仅用于拦截触摸，
                    // 避免点击穿透到小窗下方的应用界面。
                    Box(
                        Modifier
                            .fillMaxSize()
                            .pointerInput(controllerState, inPictureInPicture) {
                                detectTapGestures(
                                    onTap = {
                                        if (!inPictureInPicture) {
                                            controllerState.toggleFullVisible()
                                        }
                                    },
                                )
                            }
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
                // 滑动/拖动预览期间（手指未离开）不显示加载动画：
                // 预览会持续 seek 播放器触发缓冲（Buffering），此时显示 loading 会干扰手势操作
                if ((isLoading || isBuffering) && !progressSliderState.isPreviewing) {
                    BiliAnimTV(
                        modifier = Modifier.align(Alignment.Center),
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
                                    bottomSheetState.open(SendDanmakuPage())
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
            leftSideButtons = {
                // 小屏（非全屏）模式在画面左侧提供弹幕发送入口（与右侧锁定按钮对称）；
                // 全屏模式已有底部弹幕输入条，此处不重复显示
                if (!isFullscreen) {
                    SmallScreenDanmakuSendButton(
                        onClick = {
                            if (!userStore.isLogin()) {
                                GlobalToaster.show("请先登录")
                            } else {
                                bottomSheetState.open(SendDanmakuPage())
                            }
                        },
                    )
                }
            },
            gestureLock = {
                // 画中画窗口内不显示手势锁定按钮（锁定的其它控制器本就不显示）
                if (!inPictureInPicture) {
                    cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.GestureLock(
                        isLocked = isLocked,
                        onClick = { isLocked = !isLocked },
                    )
                }
            },
            // 顶层覆盖层：播放异常 / 播放完成（画中画窗口内不显示）
            statusOverlay = {
                if (showStatusOverlay && !inPictureInPicture) {
                    if (showErrorOverlay) {
                        PlayerErrorOverlay(
                            message = errorMessage ?: "播放出错",
                            onRetry = { playerDelegate.retry() },
                            onClose = onBack,
                            modifier = Modifier.matchParentSize(),
                        )
                    } else {
                        PlayerCompletionOverlay(
                            onReplay = { playerDelegate.replay() },
                            onClose = onBack,
                            modifier = Modifier.matchParentSize(),
                        )
                    }
                }
            },
        )
    }
}

/**
 * 小屏（非全屏）模式画面左侧的弹幕发送按钮.
 *
 * 样式与右侧手势锁定按钮（[GestureLock][cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.GestureLock]）
 * 保持一致: 半透明黑色圆角 Surface + 描边 + 白色图标.
 *
 * @param onClick 点击回调
 * @param modifier 布局修饰符
 */
@Composable
private fun SmallScreenDanmakuSendButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier.testTag(TAG_SMALL_SCREEN_DANMAKU_SEND_BUTTON).then(modifier),
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.3f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    ) {
        IconButton(onClick) {
            CompositionLocalProvider(LocalContentColor provides Color.White) {
                Icon(
                    Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "发送弹幕",
                )
            }
        }
    }
}

/**
 * 基于 [PlayerDelegateImpl] 音量的 [LevelController] 实现.
 *
 * 将手势的 0..1 归一化等级映射到播放器的 0-100 音量,
 * 与底栏音量、音量键等共用同一音量状态, 保证安卓/桌面行为一致.
 */
private class PlayerVolumeLevelController(
    private val delegate: PlayerDelegateImpl,
) : LevelController {
    override val range: ClosedRange<Float> = 0f..1f

    override val level: Float
        get() = delegate.playbackState.value.volume / 100f

    override val levelStep: Float get() = 0.01f

    override fun setLevel(level: Float) {
        delegate.setVolume((level.coerceIn(range.start, range.endInclusive) * 100).roundToInt())
    }
}
