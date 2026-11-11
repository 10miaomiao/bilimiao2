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
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.base.BottomSheetState
import cn.a10miaomiao.bilimiao.compose.common.HapticFeedbackType
import cn.a10miaomiao.bilimiao.compose.common.LocalPlayerState
import cn.a10miaomiao.bilimiao.compose.common.localPlayerDisplayMode
import cn.a10miaomiao.bilimiao.compose.common.platform.Platform
import cn.a10miaomiao.bilimiao.compose.common.platform.LocalWindowTitleBarInsets
import cn.a10miaomiao.bilimiao.compose.common.platform.platformInfo
import cn.a10miaomiao.bilimiao.compose.common.rememberHapticFeedback
import cn.a10miaomiao.bilimiao.compose.components.layout.PlayerDisplayMode
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.FastForwardIndicator
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.VideoScaffold
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.GestureIndicatorState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.LevelController
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.LockableVideoGestureHost
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.rememberBrightnessLevelController
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.rememberGestureIndicatorState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.rememberPlayerFastSkipState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.rememberSwipeSeekerState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture.rememberSystemVolumeLevelController
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.BottomProgressBar
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.MediaProgressIndicatorText
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.MediaProgressSliderDefaults
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.PlayerControllerBar
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.PlayerControllerDefaults
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.PlayerProgressSliderState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.formatSpeedNumber
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.fullscreenMenuOptions
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.rememberSmoothedProgressState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.rememberAlwaysOnRequester
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.rememberVideoControllerState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.formatSpeedValue
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.top.PlayerMenuSubMenu
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.top.PlayerMoreActionsButton
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.top.PlayerScreenType
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.top.PlayerScreenTypeButton
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.top.PlayerTopBar
import cn.a10miaomiao.bilimiao.compose.components.status.BiliAnimTV
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiEpisodesPage
import cn.a10miaomiao.bilimiao.compose.pages.player.SendDanmakuPage
import cn.a10miaomiao.bilimiao.compose.pages.playlist.PlayListPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.DanmakuDisplaySettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.VideoSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoPagesPage
import cn.a10miaomiao.bilimiao.compose.platform.LocalPlatformContext
import cn.a10miaomiao.bilimiao.compose.platform.LocalSystemBarsController
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.appDataStore
import com.a10miaomiao.bilimiao.comm.datastore.editPreferences
import com.a10miaomiao.bilimiao.comm.datastore.mapPreferences
import com.a10miaomiao.bilimiao.comm.delegate.player.BangumiPlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.DesktopFullscreenMode
import com.a10miaomiao.bilimiao.comm.delegate.player.FullscreenMode
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegateImpl
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlaybackStatus
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.SubtitleSourceInfo
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import kotlinx.coroutines.flow.map
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
 *   (经 PlatformContext 注册的系统动作按钮) 操作; 状态由 PlayerState.pictureInPicture
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
 * 长按画面的加速倍数：按住时按「当前倍速 × 该倍数」播放，松手恢复原倍速。
 * 对齐旧版（DanmakuVideoPlayer.startLongClickSpeedPlay 的 `speed *= 2`）。
 */
private const val LONG_PRESS_SPEED_FACTOR = 2.0f

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
    // 播放列表入口暂时关闭，需要时连同下方 actions 中的入口一起取消注释
//    val playListStore: PlayListStore by rememberInstance()
//    val playListState by playListStore.stateFlow.collectAsState()

    // 选集入口文案：视频看分P数量，番剧看剧集数量。
    // 对齐旧版逻辑，只有 1 P / 1 集时没有选集的意义，不显示入口。
    // 这里每次重组都重新判断（不做 remember），避免分P/剧集列表在播放源创建后才写入时取到旧值。
    val episodeEntryText = when (val source = currentSource) {
        is BangumiPlayerSource -> if (source.episodes.size > 1) "剧集" else null
        is VideoPlayerSource -> if (source.pages.size > 1) "分P" else null
        else -> null
    }

    // 选集统一用全局 BottomSheet 打开，避免打断正在进行的播放
    fun openEpisodeSheet() {
        when (val source = currentSource) {
            is BangumiPlayerSource -> bottomSheetState.open(
                BangumiEpisodesPage(
                    sid = source.sid,
                    title = source.ownerName,
                )
            )

            is VideoPlayerSource -> bottomSheetState.open(
                VideoPagesPage(aid = source.aid)
            )

            else -> {}
        }
    }

    // 清晰度可用性：1080P 以上需要大会员，480P 以上需要登录
    fun isQualityEnabled(accept: PlayerSourceInfo.AcceptInfo): Boolean = when {
        accept.quality > MAX_QUALITY_NOT_VIP -> userStore.isVip()
        accept.quality > MAX_QUALITY_NOT_LOGIN -> userStore.isLogin()
        else -> true
    }

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

    // 小屏（非全屏）底栏空间有限，清晰度 / 倍速 / 字幕收进「更多」菜单的二级菜单：
    // 一级菜单只显示当前值，点进去再选具体选项；无可用项时不生成对应入口
    val moreMenuSubMenus = buildList {
        val qualityOptions = playbackInfo?.acceptList ?: emptyList()
        if (qualityOptions.isNotEmpty()) {
            add(
                PlayerMenuSubMenu(
                    title = "清晰度",
                    value = qualityOptions.find { it.quality == currentQuality }?.description
                        ?: qualityOptions.last().description,
                ) { dismiss ->
                    PlayerControllerDefaults.QualityMenuItems(
                        currentQuality = currentQuality,
                        options = qualityOptions,
                        onValueChange = { quality ->
                            dismiss()
                            playerDelegate.changeQuality(quality)
                        },
                        isOptionEnabled = { accept -> isQualityEnabled(accept) },
                    )
                }
            )
        }
        if (speedOptions.isNotEmpty()) {
            add(
                PlayerMenuSubMenu(
                    title = "倍速",
                    value = formatSpeedValue(playbackSpeed),
                ) { dismiss ->
                    PlayerControllerDefaults.SpeedMenuItems(
                        currentSpeed = playbackSpeed,
                        options = speedOptions,
                        onValueChange = { speed ->
                            dismiss()
                            playerDelegate.setPlaybackSpeed(speed)
                        },
                    )
                }
            )
        }
        val subtitleOptions = listOf<SubtitleSourceInfo?>(null) + subtitleList
        if (subtitleOptions.size > 1) {
            add(
                PlayerMenuSubMenu(
                    title = "字幕",
                    value = currentSubtitle?.lan_doc ?: "关闭",
                ) { dismiss ->
                    PlayerControllerDefaults.SubtitleMenuItems(
                        currentSubtitle = currentSubtitle,
                        options = subtitleOptions,
                        onValueChange = { subtitle ->
                            dismiss()
                            playerDelegate.setSubtitle(subtitle)
                        },
                    )
                }
            )
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

    val playerState = LocalPlayerState.current
    // 显示模式由宿主布局判定后下发（落点是否在屏幕内等条件都已在彼处算好），
    // 这里直接沿用，避免重复判定造成两边不一致
    val displayMode = localPlayerDisplayMode()
    // 全屏状态统一由 PlayerState 提供（数据源为 FullscreenController.isFullscreen）
    val isFullscreen by playerState.fullScreenPlayer.collectAsState()
    // 全屏方式设置：安卓为屏幕方向，桌面为「铺满屏幕 / 铺满窗口」（长按全屏按钮的菜单使用）
    val fullscreenMode by playerDelegate.fullscreenController.fullscreenMode.collectAsState()
    val desktopFullscreenMode by playerDelegate.fullscreenController.desktopFullscreenMode.collectAsState()
    val desktopPlatform = platformInfo.platform == Platform.DESKTOP
    val fullMenuOptions = remember { fullscreenMenuOptions() }
    // 画中画（应用外小窗）状态：由平台层（Activity 的 onPictureInPictureModeChanged）注入，
    // 与 ComposeScaffold 共享同一状态源：宿主布局让出整个窗口，本组件负责小窗内的播放器展示
    val pictureInPicture by playerState.pictureInPicture.collectAsState()
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
    // 底部进度条显示控制（「播放设置 - 底部进度条显示控制」按位组合的三个开关，默认全关，与旧版一致）。
    // 设置项变化需要立刻生效，因此订阅 DataStore 而不是启动时读一次
    val bottomProgressBarShow by remember(appDataStore) {
        appDataStore.data.map { it[SettingPreferences.PlayerBottomProgressBarShow] ?: 0 }
    }.collectAsState(initial = 0)
    // 按当前展示方式取对应的开关位：全屏、画中画各自独立，其余（嵌入竖屏/悬浮横屏/锚点浮层）算作小屏
    val bottomProgressBarEnabled = when (displayMode) {
        PlayerDisplayMode.PictureInPicture ->
            (bottomProgressBarShow and SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_PIP) != 0

        PlayerDisplayMode.Fullscreen ->
            (bottomProgressBarShow and SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_FULL) != 0

        else ->
            (bottomProgressBarShow and SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_SMALL) != 0
    }
    // 悬浮横屏模式关闭完整手势操作（拖动/缩放由外层悬浮窗口处理），仅保留单击切换控制器；
    // 画中画窗口内控制器恒隐藏，任何播放器手势都无意义，同样关闭
    val gesturesEnabled = displayMode != PlayerDisplayMode.FloatingLandscape &&
        !inPictureInPicture
    // 桌面端「铺满窗口」时窗口保持带标题栏的普通窗口，标题栏叠在内容上方且占用了拖动区域，
    // 播放器内容必须让开它（否则顶栏按钮会落在标题栏里点不到）。
    // 与安全区取最大值，避免 safeDrawing 已经包含标题栏时重复叠加。
    val windowTitleBarInsets = LocalWindowTitleBarInsets.current
    val contentWindowInsets = if (isFullscreen) {
        val safe = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
        if (desktopPlatform && desktopFullscreenMode == DesktopFullscreenMode.WINDOW) {
            val density = LocalDensity.current
            // 左右沿用安全区，顶部取「安全区顶部」与「标题栏高度」的较大值
            val topPx = maxOf(safe.getTop(density), windowTitleBarInsets.getTop(density))
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).union(
                WindowInsets(
                    left = 0.dp,
                    top = with(density) { topPx.toDp() },
                    right = 0.dp,
                    bottom = 0.dp,
                )
            )
        } else {
            safe
        }
    } else {
        WindowInsets(0.dp)
    }

    val controllerState = rememberVideoControllerState()
    // 长按全屏按钮弹出的方向菜单展开期间保持控制器常显，避免控制器自动隐藏把菜单一起收走
    val fullModeMenuAlwaysOnRequester = rememberAlwaysOnRequester(controllerState, "fullscreenModeMenu")
    // 「更多」菜单（含二级菜单）展开期间同样需要保持常显
    val moreMenuAlwaysOnRequester = rememberAlwaysOnRequester(controllerState, "moreMenu")
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

    // 仅在「正在播放」期间保持屏幕常亮（与是否全屏无关），恢复系统屏幕超时：
    // - Playing / Buffering 置位：Buffering 由 mediamp 的 PAUSED_BUFFERING 映射而来，
    //   语义是 playWhenReady && isBuffering（用户意图播放、正在等数据），即播放中的卡顿缓冲；
    //   用户主动暂停永远不会落进这一分支（mediamp 的 PAUSED = !playWhenReady）。
    // - 暂停 / 播放完成 / 出错 / 加载中一律清除，避免暂停或挂着完成界面时白耗电。
    // 窗口能力经 compose 层 PlatformContext 取得：安卓端实现由 MainActivity 以自身窗口
    // 创建并随 Activity 重建更新，无需全局持有 Activity。
    val platformContext = LocalPlatformContext.current
    val keepScreenOn = isPlaying || isBuffering
    DisposableEffect(platformContext, keepScreenOn) {
        platformContext.setKeepScreenOn(keepScreenOn)
        onDispose { platformContext.setKeepScreenOn(false) }
    }

    // 画中画动作按钮（播放/暂停）：画中画窗口内的画面不接收点击，播放控制只能交给系统绘制。
    // 进入画中画时注册动作按钮点击处理，播放状态变化时同步按钮图标，退出画中画时注销。
    // 注册/注销与 PiP 参数同步均走 compose 层 PlatformContext（安卓实现持有当前 Activity 窗口）。
    val piPTogglePlayPause = rememberUpdatedState<() -> Unit> {
        if (isPlaying) playerDelegate.pause() else playerDelegate.resume()
    }
    LaunchedEffect(platformContext, inPictureInPicture, isPlaying) {
        if (inPictureInPicture) platformContext.setPictureInPictureActions(isPlaying)
    }
    DisposableEffect(platformContext, inPictureInPicture) {
        if (!inPictureInPicture) return@DisposableEffect onDispose {}
        val unregister = platformContext.registerPictureInPictureActionReceiver {
            piPTogglePlayPause.value()
        }
        onDispose { unregister() }
    }

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

        // 缓动进度：播放位置是 200ms 轮询采样，逐帧外推后再画，底部进度条才是平滑前进而不是一跳一跳。
        // 状态一律从 delegate 实时读取（不是组合期快照），暂停/缓冲/倍速变化才能立即生效。
        val smoothedProgress = rememberSmoothedProgressState(
            positionMillis = { playerDelegate.currentPosition.value },
            durationMillis = { currentDuration },
            isPlaying = { playerDelegate.playbackState.value.status == PlaybackStatus.Playing },
            playbackSpeed = { playerDelegate.playbackState.value.playbackSpeed },
        )

        // 长按画面加速状态（手势宿主与悬浮提示共用）：
        // 按住时按当前倍速 ×[LONG_PRESS_SPEED_FACTOR] 播放，松手恢复原倍速；仅播放中响应
        val hapticFeedback = rememberHapticFeedback()
        var speedBeforeFastSkip by remember { mutableFloatStateOf(playbackSpeed) }
        var fastForwarding by remember { mutableStateOf(false) }
        var fastForwardSpeed by remember { mutableFloatStateOf(0f) }
        val fastSkipState = rememberPlayerFastSkipState(
            gestureIndicatorState = indicatorState,
            onStart = {
                // 直接读 delegate 的实时倍速，而不是组合期捕获的 playbackSpeed 快照，
                // 保证 2.5x/3x/4x 等自定义倍速下长按是「该倍速 ×2」，松手也回到该倍速
                val baseSpeed = playerDelegate.playbackState.value.playbackSpeed
                speedBeforeFastSkip = baseSpeed
                val skipSpeed = baseSpeed * LONG_PRESS_SPEED_FACTOR
                fastForwardSpeed = skipSpeed
                playerDelegate.setPlaybackSpeed(skipSpeed)
                fastForwarding = true
                // 长按触发震动反馈（对齐旧版 performHapticFeedback(LONG_PRESS)）
                hapticFeedback.perform(HapticFeedbackType.LONG_PRESS)
            },
            onStop = {
                playerDelegate.setPlaybackSpeed(speedBeforeFastSkip)
                fastForwarding = false
            },
        )

        val enter = fadeIn()
        val exit = fadeOut()

        // 音量/亮度手势控制器:
        // - 音量 = 系统媒体音量 (安卓直接调 AudioManager, 与硬件音量键一致);
        //   桌面端不接管系统音量, 回退到播放器自身音量 (PlayerDelegate.setVolume)
        // - 亮度 = 平台窗口亮度 (安卓生效; 桌面不支持逐窗口亮度, 为 no-op)
        val playerVolumeController = remember(playerDelegate) { PlayerVolumeLevelController(playerDelegate) }
        val audioController = rememberSystemVolumeLevelController() ?: playerVolumeController
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
                        // 小屏（非全屏）播放器顶栏空间有限，选集/播放列表入口只在全屏控制器中显示
                        if (isFullscreen) {
                            // 选集入口（分P / 剧集），对齐旧版播放器顶栏的拓展按钮
                            episodeEntryText?.let { text ->
                                PlayerControllerDefaults.SelectEpisodeIcon(
                                    text = text,
                                    onClick = { openEpisodeSheet() },
                                )
                            }
                            // 当前播放列表入口（列表非空时才显示），同样用全局 BottomSheet 打开。
                            // 暂时关闭，需要时取消注释
//                            if (playListState.items.isNotEmpty()) {
//                                IconButton(
//                                    onClick = { bottomSheetState.open(PlayListPage()) },
//                                ) {
//                                    Icon(
//                                        Icons.AutoMirrored.Rounded.PlaylistPlay,
//                                        "播放列表",
//                                        tint = LocalContentColor.current,
//                                    )
//                                }
//                            }
                        }
                        // 画中画（仅安卓 8.0+ 支持，桌面端暂不支持）
                        if (platformContext.isPictureInPictureSupported()) {
                            IconButton(
                                onClick = {
                                    val info = playbackInfo
                                    val width = info?.width ?: 16
                                    val height = info?.height ?: 9
                                    // 初始动作按钮按当前播放状态给出「暂停」或「播放」
                                    if (!platformContext.enterPictureInPictureMode(width, height, isPlaying)) {
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
                            // 小屏（非全屏）底栏空间有限：清晰度 / 倍速 / 字幕作为「更多」菜单的二级菜单；
                            // 全屏底栏已有这些入口，不再重复
                            subMenus = if (isFullscreen) {
                                emptyList()
                            } else {
                                moreMenuSubMenus
                            },
                            onExpandedChanged = { expanded ->
                                if (expanded) {
                                    moreMenuAlwaysOnRequester.request()
                                } else {
                                    moreMenuAlwaysOnRequester.cancelRequest()
                                }
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
                    // 每帧绘制前直接从播放器取进度：不经过 Compose 状态，避免重组延迟让弹幕迟到
                    positionProvider = { playerDelegate.currentPosition.value },
                    isPlaying = isPlaying,
                    danmakuParser = danmakuParser,
                    // 播放器显式通知 seek（拖动进度条、切清晰度恢复、重播），弹幕层不做推断
                    seekFlow = playerDelegate.seekFlow,
                    // 倍速只提高弹幕出场密度，弹幕滚动速度与停留时长保持恒定
                    playbackSpeed = playbackSpeed,
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
                        // 只有播放中才响应长按加速（暂停/播放结束/出错时不响应），
                        // 长按期间播放中断时该开关变化会摘除手势并自动恢复原倍速
                        fastSkipEnabled = isPlaying,
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
                // 长按倍速浮窗（样式对齐旧版，提示「x倍速播放中」）
                if (fastForwarding) {
                    Box(
                        Modifier.align(Alignment.TopCenter)
                            .padding(top = 40.dp)
                    ) {
                        FastForwardIndicator(
                            modifier = Modifier.align(Alignment.TopCenter),
                            text = "${formatSpeedNumber(fastForwardSpeed)}倍速播放中",
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
                                isOptionEnabled = { accept -> isQualityEnabled(accept) },
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
                        // 全屏按钮（长按弹出全屏方式菜单，对照旧版长按全屏按钮：
                        // 安卓选择屏幕方向，桌面选择铺满屏幕/铺满窗口）
                        PlayerControllerDefaults.FullscreenIcon(
                            isFullscreen = isFullscreen,
                            onClickFullscreen = onToggleFullscreen,
                            menuOptions = fullMenuOptions,
                            currentMenuValue = if (desktopPlatform) {
                                desktopFullscreenMode.value
                            } else {
                                fullscreenMode.value
                            },
                            onSelectMenuValue = { value ->
                                if (desktopPlatform) {
                                    playerDelegate.fullscreenController.setDesktopFullscreenMode(
                                        DesktopFullscreenMode.fromValue(value)
                                    )
                                } else {
                                    playerDelegate.fullscreenController.setFullscreenMode(
                                        FullscreenMode.fromValue(value)
                                    )
                                }
                            },
                            onModeMenuExpandedChanged = { expanded ->
                                if (expanded) {
                                    fullModeMenuAlwaysOnRequester.request()
                                } else {
                                    fullModeMenuAlwaysOnRequester.cancelRequest()
                                }
                            },
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
            bottomProgressBar = {
                // 底部细进度条：控制器隐藏时显示播放进度（旧版 mBottomProgressBar 的行为）。
                // 用逐帧平滑位置绘制，与底栏进度滑块共享同一状态，拖动预览时跟手显示
                if (bottomProgressBarEnabled) {
                    BottomProgressBar(
                        state = progressSliderState,
                        smoothedProgress = smoothedProgress,
                    )
                }
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
 * 与底栏音量、音量键等共用同一音量状态.
 *
 * 仅在平台不提供系统音量控制时使用 (见 [rememberSystemVolumeLevelController]):
 * 安卓端手势直接调节系统媒体音量, 桌面端才回退到这里的播放器音量.
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
