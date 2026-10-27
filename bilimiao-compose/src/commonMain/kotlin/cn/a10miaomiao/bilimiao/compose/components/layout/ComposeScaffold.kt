package cn.a10miaomiao.bilimiao.compose.components.layout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import cn.a10miaomiao.bilimiao.compose.ORIENTATION_LANDSCAPE
import cn.a10miaomiao.bilimiao.compose.ORIENTATION_PORTRAIT
import cn.a10miaomiao.bilimiao.compose.LocalAppDarkTheme
import cn.a10miaomiao.bilimiao.compose.PlayerState
import cn.a10miaomiao.bilimiao.compose.StartViewState

import cn.a10miaomiao.bilimiao.compose.common.LocalContentInsets
import cn.a10miaomiao.bilimiao.compose.common.LocalContentVisibleSize
import cn.a10miaomiao.bilimiao.compose.common.LocalPlayerState
import cn.a10miaomiao.bilimiao.compose.common.LocalPlayerDisplayMode
import cn.a10miaomiao.bilimiao.compose.common.isCompactWindow
import cn.a10miaomiao.bilimiao.compose.common.ContentInsets
import cn.a10miaomiao.bilimiao.compose.common.toContentInsets
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBar
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarConfig
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarHorizontal
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarOrientation
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarState
import cn.a10miaomiao.bilimiao.compose.platform.LocalSystemBarsController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private const val DrawerMaxWidth = 400
private const val DrawerScrimMaxAlpha = 0.4f
private const val DrawerSettleDurationMillis = 250
private const val DrawerVelocityThresholdFraction = 0.1f

private enum class ComposeDrawerValue {
    Closed,
    Open,
}

/** 跨帧记录用的可变持有对象：只在测量阶段读写，不参与重组通知 */
private class MeasuredFlag(var value: Boolean = false)

@Composable
fun ComposeScaffold(
    startViewState: StartViewState,
    playerContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    appBarState: AppBarState? = null,
    allowDrawerOpenGesture: Boolean = true,
    drawerContent: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val playerState = startViewState.playerState
    val showPlayer = playerState.showPlayer
    val fullScreenPlayer by playerState.fullScreenPlayer.collectAsState()
    // 画中画（应用外小窗）：由平台层注入的状态流，BiliVideoScaffold 内的画中画按钮触发；
    // 本组件据此让出整个窗口（隐藏 appbar/内容区），交由播放器独占小窗
    val pictureInPicture by playerState.pictureInPicture.collectAsState()
    val orientation = if (isCompactWindow()) ORIENTATION_PORTRAIT else ORIENTATION_LANDSCAPE
    val portraitPlayerLayoutState = playerState.portraitPlayerLayoutState
    val floatingPlayerLayoutState = playerState.floatingPlayerLayoutState
    val playerVideoRatio = playerState.playerVideoRatio
    // 播放器落点：启用位 + 矩形一并交给显示模式判定，
    // 由判定函数结合宿主视口判断落点是否还在屏幕内（滚出屏幕后不再按锚点布局）
    val anchorEnabled = if (showPlayer) playerState.anchorEnabled else false
    val anchorBounds = if (anchorEnabled) playerState.anchorBounds else null
    val drawerState = startViewState.drawerState
    // 宿主布局在根坐标系中的可视矩形：内容区按其判断落点是否还在屏幕内。
    // 不用 maxWidth/maxHeight 直接换算，是因为窗口可能有自绘边框/标题栏，
    // 锚点容器上报的是 positionInRoot，两者必须用同一坐标系
    var viewportRect by remember { mutableStateOf(Rect.Zero) }

    // 播放器显示模式（唯一判定源，与 ComposeScaffoldPlayerLayoutState/PlayerLayer 共用，
    // 见 calculatePlayerDisplayMode）
    val playerDisplayMode = calculatePlayerDisplayMode(
        showPlayer = showPlayer,
        fullScreenPlayer = fullScreenPlayer,
        pictureInPicture = pictureInPicture,
        anchorEnabled = anchorEnabled,
        anchorBounds = anchorBounds,
        viewport = viewportRect,
        orientation = orientation,
    )
    // 状态栏前景色统一由本组件控制，平台实现经 LocalSystemBarsController 注入；
    // 全屏播放器（BiliVideoScaffold）只经由同一控制器控制系统栏显隐，不触碰前景色：
    // - 深色模式：始终白色图标；
    // - 浅色模式：仅 EmbeddedPortrait / Fullscreen / PictureInPicture（播放器画面为深色底）
    //   为白色图标，其余黑色图标。
    val systemBarsController = LocalSystemBarsController.current
    val isDarkTheme = LocalAppDarkTheme.current
    LaunchedEffect(playerDisplayMode, isDarkTheme) {
        val lightIcons = !isDarkTheme &&
            playerDisplayMode != PlayerDisplayMode.EmbeddedPortrait &&
            playerDisplayMode != PlayerDisplayMode.Fullscreen &&
            playerDisplayMode != PlayerDisplayMode.PictureInPicture
        systemBarsController.setLightStatusBar(lightIcons)
    }

    val density = LocalDensity.current
    val appBarNestedScrollConnection = remember(appBarState, density, orientation) {
        if (appBarState == null) {
            null
        } else {
            val collapseRangePx = with(density) { AppBarConfig.MenuHeight.toPx() }
            object : NestedScrollConnection {
                // 横屏 AppBar 是侧边栏，不参与滚动收缩
                private val enabled: Boolean
                    get() = appBarState.visible &&
                        appBarState.orientation == AppBarOrientation.Vertical

                override fun onPostScroll(
                    consumed: androidx.compose.ui.geometry.Offset,
                    available: androidx.compose.ui.geometry.Offset,
                    source: NestedScrollSource,
                ): androidx.compose.ui.geometry.Offset {
                    if (!enabled) {
                        return androidx.compose.ui.geometry.Offset.Zero
                    }
                    // consumed、available 之和即本次用户滑动位移：内容向上滑（y < 0）收起 AppBar。
                    // 这里不消费滚动量，列表照常滚动，AppBar 只是跟随位移联动
                    val scrollDelta = consumed.y + available.y
                    if (scrollDelta == 0f) {
                        return androidx.compose.ui.geometry.Offset.Zero
                    }
                    appBarState.scrollCollapse(-scrollDelta, collapseRangePx)
                    return androidx.compose.ui.geometry.Offset.Zero
                }

                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity,
                ): Velocity {
                    if (enabled) {
                        // 惯性结束后按速度/过半阈值吸附到展开或收起
                        appBarState.settleCollapse(available.y, collapseRangePx)
                    }
                    return Velocity.Zero
                }
            }
        }
    }

    LaunchedEffect(appBarState?.orientation) {
        // 切到横屏或返回竖屏时恢复展开状态（竖屏收缩只在滚动中产生）
        appBarState?.expandAppBar()
    }

    val rawWindowInsets = WindowInsets.safeDrawing.toContentInsets()
    val densityFloat = density.density
    // 播放器上一帧是否在显示：测量阶段据此区分「首次出现」与「切换落点」——
    // 首次出现才需要等落点上报，已显示时保持原几何以便播放模式切换动画。
    // 普通持有对象，不触发重组
    val playerVisible = remember { MeasuredFlag() }
    val playerLayoutState = remember(
        showPlayer,
        fullScreenPlayer,
        pictureInPicture,
        orientation,
        portraitPlayerLayoutState,
        floatingPlayerLayoutState,
        playerVideoRatio,
        anchorEnabled,
        anchorBounds,
        viewportRect,
    ) {
        ComposeScaffoldPlayerLayoutState(
            showPlayer = showPlayer,
            fullScreenPlayer = fullScreenPlayer,
            orientation = orientation,
            portraitState = portraitPlayerLayoutState,
            floatingState = floatingPlayerLayoutState,
            playerVideoRatio = playerVideoRatio,
            anchorEnabled = anchorEnabled,
            anchorBounds = anchorBounds,
            viewport = viewportRect,
            pictureInPicture = pictureInPicture,
        )
    }
    val drawerController = rememberComposeDrawerController(
        initialState = drawerState,
        onStateChanged = startViewState::setDrawerState,
    )
    var drawerMeasuredWidthPx by remember(orientation) { mutableIntStateOf(0) }

    LaunchedEffect(drawerMeasuredWidthPx) {
        drawerController.updateDrawerWidth(drawerMeasuredWidthPx.toFloat())
    }

    LaunchedEffect(drawerState, drawerMeasuredWidthPx) {
        if (drawerMeasuredWidthPx <= 0) {
            return@LaunchedEffect
        }
        when (drawerState) {
            StartViewState.DRAWER_STATE_EXPANDED -> drawerController.open()
            StartViewState.DRAWER_STATE_COLLAPSED -> drawerController.close()
        }
    }

    LaunchedEffect(
        drawerController.openFraction,
        drawerController.currentValue,
        drawerController.targetValue,
    ) {
        drawerController.syncWrapperState()
    }

    // 宿主布局在根坐标系中的可视矩形：内容区据此判断播放器落点是否还在屏幕内
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                val size = coordinates.size
                val rect = Rect(
                    left = position.x,
                    top = position.y,
                    right = position.x + size.width,
                    bottom = position.y + size.height,
                )
                if (viewportRect != rect) {
                    viewportRect = rect
                }
            }
    ) {
        val layoutResult = with(density) {
            calculateComposeScaffoldLayout(
                viewportWidth = maxWidth,
                viewportHeight = maxHeight,
                rawWindowInsets = rawWindowInsets,
                appBarState = appBarState,
                playerState = playerLayoutState,
            )
        }
        val viewportWidth = maxWidth
        val viewportHeight = maxHeight
        val maxHeightPx = with(density) { maxHeight.toPx() }
        // 内容区可视尺寸：页面上「不超过一屏」的内容（如详情页封面）据此限制高度
        val contentVisibleSize = DpSize(
            width = (maxWidth - layoutResult.contentInsets.left - layoutResult.contentInsets.right)
                .coerceAtLeast(0.dp),
            height = (maxHeight - layoutResult.contentInsets.top - layoutResult.contentInsets.bottom)
                .coerceAtLeast(0.dp),
        )
        val leftEdgeWidthPx = with(density) { 40.dp.toPx() }
        val appBarGestureRect = layoutResult.appBarHorizontalBounds ?: layoutResult.appBarVerticalBounds
        val drawerWidth = if (maxWidth > DrawerMaxWidth.dp) DrawerMaxWidth.dp else maxWidth
        val scrimAlpha = drawerController.openFraction * DrawerScrimMaxAlpha
        val contentOffsetY = remember { Animatable(0f) }

        LaunchedEffect(layoutResult.contentBounds.top) {
            contentOffsetY.animateTo(
                targetValue = layoutResult.contentBounds.top,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
                ),
            )
        }
        val drawerGestureModifier = Modifier.anchoredDraggable(
            state = drawerController.state,
            orientation = Orientation.Horizontal,
            enabled = true,
            flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                state = drawerController.state,
                positionalThreshold = { distance -> distance * 0.5f },
            ),
        )
        val appBarDrawerGestureModifier = if (
            allowDrawerOpenGesture && drawerController.settledValue == ComposeDrawerValue.Closed
        ) {
            Modifier
                .pointerInput(startViewState) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        startViewState.setTouchStartTop(down.position.y, maxHeightPx.toInt(), densityFloat)
                    }
                }
                .then(drawerGestureModifier)
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(
                    allowDrawerOpenGesture,
                    drawerController.settledValue,
                    leftEdgeWidthPx,
                    appBarGestureRect,
                ) {
                    if (!allowDrawerOpenGesture || drawerController.settledValue != ComposeDrawerValue.Closed) {
                        return@pointerInput
                    }
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        if (!isDrawerOpenGestureStart(down.position, leftEdgeWidthPx, appBarGestureRect)) {
                            return@awaitEachGesture
                        }
                        startViewState.setTouchStartTop(down.position.y, maxHeightPx.toInt(), densityFloat)
                        val pointerId = down.id
                        val velocityTracker = VelocityTracker()
                        velocityTracker.addPosition(down.uptimeMillis, down.position)
                        var dragging = false
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (!change.pressed) {
                                if (dragging) {
                                    drawerController.scope.launch {
                                        drawerController.settle(velocityTracker.calculateVelocity().x)
                                    }
                                }
                                break
                            }
                            if (!change.positionChanged()) {
                                continue
                            }
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            val deltaX = change.position.x - down.position.x
                            val deltaY = change.position.y - down.position.y
                            if (!dragging) {
                                if (kotlin.math.abs(deltaY) > kotlin.math.abs(deltaX) || deltaX <= 0f) {
                                    break
                                }
                                dragging = true
                            }
                            val consumed = drawerController.state.dispatchRawDelta(deltaX - drawerController.dragConsumedDelta)
                            drawerController.dragConsumedDelta += consumed
                            if (consumed != 0f) {
                                change.consume()
                            }
                        }
                        drawerController.dragConsumedDelta = 0f
                    }
                }
        ) {
            SubcomposeLayout(modifier = Modifier.fillMaxSize()) { constraints ->
                val contentPlaceable = subcompose("content") {
                    CompositionLocalProvider(
                        LocalContentInsets provides layoutResult.contentInsets,
                        LocalPlayerState provides playerState,
                        LocalContentVisibleSize provides contentVisibleSize,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (appBarNestedScrollConnection != null) {
                                        Modifier.nestedScroll(appBarNestedScrollConnection)
                                    } else {
                                        Modifier
                                    }
                                )
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            content()
                        }
                    }
                }.single().measure(contentConstraints(constraints, layoutResult.contentBounds))

                val verticalAppBarPlaceable = if (layoutResult.hasVerticalAppBar && appBarState != null) {
                    subcompose("appBarVertical") {
                        AppBar(
                            title = appBarState.title,
                            canBack = appBarState.canBack,
                            showPointer = appBarState.showPointer,
                            pointerOrientation = appBarState.pointerOrientation,
                            showExchange = appBarState.showExchange,
                            menus = appBarState.menus,
                            isNavigationMenu = appBarState.isNavigationMenu,
                            checkedKey = appBarState.checkedKey,
                            appBarState = appBarState,
                            onBackClick = { appBarState._onBackClick?.invoke() },
                            onMenuClick = { appBarState._onMenuClick?.invoke() },
                            onMenuItemClick = { appBarState._onMenuItemClick?.invoke(it) },
                            onPointerClick = { appBarState._onPointerClick?.invoke() },
                            onExchangeClick = { appBarState._onExchangeClick?.invoke() },
                            modifier = Modifier
                                .fillMaxSize()
                                .then(appBarDrawerGestureModifier),
                        )
                    }.single().measure(boundsConstraints(layoutResult.appBarVerticalBounds!!))
                } else {
                    null
                }

                val horizontalAppBarPlaceable = if (layoutResult.hasHorizontalAppBar && appBarState != null) {
                    subcompose("appBarHorizontal") {
                        AppBarHorizontal(
                            title = appBarState.title,
                            showBack = appBarState.canBack,
                            showPointer = appBarState.showPointer,
                            pointerOrientation = appBarState.pointerOrientation,
                            showExchange = appBarState.showExchange,
                            menus = appBarState.menus,
                            isNavigationMenu = appBarState.isNavigationMenu,
                            checkedKey = appBarState.checkedKey,
                            appBarState = appBarState,
                            onBackClick = { appBarState._onBackClick?.invoke() },
                            onMenuClick = { appBarState._onMenuClick?.invoke() },
                            onMenuItemClick = { appBarState._onMenuItemClick?.invoke(it) },
                            onPointerClick = { appBarState._onPointerClick?.invoke() },
                            onExchangeClick = { appBarState._onExchangeClick?.invoke() },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }.single().measure(boundsConstraints(layoutResult.appBarHorizontalBounds!!))
                } else {
                    null
                }

                // 锚点容器在组合时已上报「落点是否启用」，矩形要等本帧布局结束才上报，因此：
                // - 已启用且有矩形：直接按锚点算几何（落点是否在屏幕内由判定函数处理）；
                // - 已启用但矩形还没到：只有「播放器当前没显示」时才等（如详情页加载完立即自动播放），
                //   保持不显示、下一帧直接落在锚点；播放器已经在显示（如悬浮窗）时保持原样，
                //   等矩形到了再滑过去——否则会先把播放器销毁重建，模式切换动画就丢了；
                // - 未启用（本页没有落点容器，或当前播放的不是本页视频）：照常按窗口方向布局。
                val measuredAnchorEnabled = if (showPlayer) playerState.anchorEnabled else false
                val measuredAnchorBounds = if (measuredAnchorEnabled) playerState.anchorBounds else null
                val anchorPending = measuredAnchorEnabled &&
                    measuredAnchorBounds == null &&
                    !playerVisible.value &&
                    !fullScreenPlayer &&
                    !pictureInPicture &&
                    orientation == ORIENTATION_LANDSCAPE
                val measuredPlayerState = playerLayoutState.copy(
                    anchorEnabled = measuredAnchorEnabled,
                    anchorBounds = measuredAnchorBounds,
                    showPlayer = showPlayer && !anchorPending,
                )
                val measuredPlayerBounds = with(density) {
                    calculatePlayerBounds(
                        viewportWidth = viewportWidth,
                        viewportHeight = viewportHeight,
                        playerState = measuredPlayerState,
                        anchorBounds = measuredAnchorBounds,
                    )
                }
                playerVisible.value = measuredPlayerBounds != null

                val playerPlaceable = if (measuredPlayerBounds != null) {
                    subcompose("player") {
                        PlayerLayer(
                            playerState = playerState,
                            displayMode = measuredPlayerState.displayMode,
                            playerContent = playerContent,
                            baseBounds = measuredPlayerBounds,
                            portraitTopInset = rawWindowInsets.top,
                            safeDrawingInsets = rawWindowInsets,
                            viewportWidth = viewportWidth,
                            viewportHeight = viewportHeight,
                        )
                    }.single().measure(
                        constraints.copy(
                            minWidth = 0,
                            minHeight = 0,
                        )
                    )
                } else {
                    null
                }

                layout(constraints.maxWidth, constraints.maxHeight) {
                    val contentRect = layoutResult.contentBounds
                    contentPlaceable.placeRelative(
                        contentRect.left.toInt(),
                        contentOffsetY.value.toInt(),
                    )

                    layoutResult.appBarHorizontalBounds?.let { rect ->
                        horizontalAppBarPlaceable?.placeRelative(rect.left.toInt(), rect.top.toInt())
                    }
                    layoutResult.appBarVerticalBounds?.let { rect ->
                        verticalAppBarPlaceable?.placeRelative(rect.left.toInt(), rect.top.toInt())
                    }
                    // 播放器始终铺满 viewport 测量并放在 (0, 0)，
                    // 实际位置/尺寸由 PlayerLayer 内部动画 offset 控制，模式切换时可平滑滑行
                    playerPlaceable?.placeRelative(0, 0)
                }
            }

            if (drawerController.isOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(drawerGestureModifier)
                        .background(Color.Black.copy(alpha = scrimAlpha))
                        .pointerInput(Unit) {
                            detectTapGestures {
                                drawerController.scope.launch {
                                    drawerController.close()
                                }
                            }
                        }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(drawerWidth)
                    .offset {
                        IntOffset(drawerController.currentOffset.roundToInt(), 0)
                    }
                    .then(
                        if (drawerMeasuredWidthPx == 0) Modifier.graphicsLayer { alpha = 0f } else Modifier
                    )
                    .onSizeChanged {
                        drawerMeasuredWidthPx = it.width
                    }
                    .then(drawerGestureModifier)
            ) {
                drawerContent()
            }
        }
    }
}

private fun isDrawerOpenGestureStart(
    position: Offset,
    leftEdgeWidthPx: Float,
    appBarGestureRect: Rect?,
): Boolean {
    return position.x <= leftEdgeWidthPx || appBarGestureRect?.contains(position) == true
}

private class ComposeDrawerController(
    initialState: Int,
    private val onStateChanged: (Int) -> Unit,
    val scope: CoroutineScope,
) {
    private var drawerWidthPx by mutableFloatStateOf(0f)
    private var lastDispatchedState by mutableIntStateOf(initialState)
    private var programmaticChange by mutableStateOf(false)
    var dragConsumedDelta by mutableFloatStateOf(0f)

    val state = AnchoredDraggableState(
        initialValue = initialState.toDrawerValue(),
    )

    val currentValue: ComposeDrawerValue
        get() = state.currentValue

    val targetValue: ComposeDrawerValue
        get() = state.targetValue

    val settledValue: ComposeDrawerValue
        get() = state.settledValue

    val isAnimationRunning: Boolean
        get() = state.isAnimationRunning

    val currentOffset: Float
        get() = if (drawerWidthPx <= 0f || state.offset.isNaN()) -drawerWidthPx else state.requireOffset()

    val openFraction: Float
        get() = if (drawerWidthPx <= 0f) 0f else ((drawerWidthPx + currentOffset) / drawerWidthPx).coerceIn(0f, 1f)

    val isOpen: Boolean
        get() = openFraction > 0f || settledValue == ComposeDrawerValue.Open || isAnimationRunning

    suspend fun updateDrawerWidth(widthPx: Float) {
        if (widthPx <= 0f || drawerWidthPx == widthPx) {
            return
        }
        drawerWidthPx = widthPx
        state.updateAnchors(
            DraggableAnchors {
                ComposeDrawerValue.Closed at -drawerWidthPx
                ComposeDrawerValue.Open at 0f
            }
        )
    }

    suspend fun open() {
        if (drawerWidthPx <= 0f || targetValue == ComposeDrawerValue.Open) {
            return
        }
        programmaticChange = true
        state.animateTo(
            ComposeDrawerValue.Open,
            animationSpec = tween(durationMillis = DrawerSettleDurationMillis),
        )
        programmaticChange = false
        dispatchState(StartViewState.DRAWER_STATE_EXPANDED)
    }

    suspend fun close() {
        if (drawerWidthPx <= 0f || targetValue == ComposeDrawerValue.Closed) {
            return
        }
        programmaticChange = true
        state.animateTo(
            ComposeDrawerValue.Closed,
            animationSpec = tween(durationMillis = DrawerSettleDurationMillis),
        )
        programmaticChange = false
        dispatchState(StartViewState.DRAWER_STATE_COLLAPSED)
    }

    suspend fun settle(velocity: Float) {
        if (drawerWidthPx <= 0f) {
            return
        }
        val threshold = drawerWidthPx * DrawerVelocityThresholdFraction
        val target = when {
            velocity >= threshold -> ComposeDrawerValue.Open
            velocity <= -threshold -> ComposeDrawerValue.Closed
            openFraction >= 0.5f -> ComposeDrawerValue.Open
            else -> ComposeDrawerValue.Closed
        }
        programmaticChange = true
        dispatchState(StartViewState.DRAWER_STATE_SETTLING)
        state.animateTo(
            target,
            animationSpec = tween(durationMillis = DrawerSettleDurationMillis),
        )
        programmaticChange = false
        dispatchState(
            if (target == ComposeDrawerValue.Open) {
                StartViewState.DRAWER_STATE_EXPANDED
            } else {
                StartViewState.DRAWER_STATE_COLLAPSED
            }
        )
    }

    fun syncWrapperState() {
        if (programmaticChange) {
            return
        }
        when {
            isAnimationRunning -> {
                dispatchState(StartViewState.DRAWER_STATE_SETTLING)
            }
            settledValue == ComposeDrawerValue.Open && targetValue == ComposeDrawerValue.Open -> {
                dispatchState(StartViewState.DRAWER_STATE_EXPANDED)
            }
            settledValue == ComposeDrawerValue.Closed && targetValue == ComposeDrawerValue.Closed -> {
                dispatchState(StartViewState.DRAWER_STATE_COLLAPSED)
            }
            else -> {
                dispatchState(StartViewState.DRAWER_STATE_DRAGGING)
            }
        }
    }

    private fun dispatchState(state: Int) {
        if (lastDispatchedState == state) {
            return
        }
        lastDispatchedState = state
        onStateChanged(state)
    }
}

@Composable
private fun rememberComposeDrawerController(
    initialState: Int,
    onStateChanged: (Int) -> Unit,
): ComposeDrawerController {
    val scope = rememberCoroutineScope()
    return remember(scope, onStateChanged) {
        ComposeDrawerController(
            initialState = initialState,
            onStateChanged = onStateChanged,
            scope = scope,
        )
    }
}

private fun Int.toDrawerValue(): ComposeDrawerValue {
    return if (this == StartViewState.DRAWER_STATE_EXPANDED) {
        ComposeDrawerValue.Open
    } else {
        ComposeDrawerValue.Closed
    }
}

private fun boundsConstraints(bounds: Rect): Constraints {
    val width = bounds.width.toInt().coerceAtLeast(0)
    val height = bounds.height.toInt().coerceAtLeast(0)
    return Constraints.fixed(width, height)
}

private fun contentConstraints(rootConstraints: Constraints, bounds: Rect): Constraints {
    val width = bounds.width.toInt().coerceAtLeast(0)
    val height = bounds.height.toInt().coerceAtLeast(0)
    return rootConstraints.copy(
        minWidth = width,
        maxWidth = width,
        minHeight = height,
        maxHeight = height,
    )
}

@Composable
internal fun PlayerLayer(
    playerState: PlayerState,
    displayMode: PlayerDisplayMode,
    playerContent: (@Composable () -> Unit)?,
    baseBounds: Rect,
    portraitTopInset: Dp,
    safeDrawingInsets: ContentInsets = ContentInsets(),
    viewportWidth: Dp = 0.dp,
    viewportHeight: Dp = 0.dp,
) {
    val density = LocalDensity.current

    if (playerContent == null) {
        return
    }

    val screenWidth = viewportWidth
    val screenHeight = viewportHeight

    fun clampFloatingOffset(
        ox: Dp,
        oy: Dp,
        w: Dp,
        h: Dp,
    ): Pair<Dp, Dp> {
        val minX = safeDrawingInsets.left
        val minY = safeDrawingInsets.top
        val maxX = (screenWidth - safeDrawingInsets.right - w).coerceAtLeast(minX)
        val maxY = (screenHeight - safeDrawingInsets.bottom - h).coerceAtLeast(minY)
        return ox.coerceIn(minX, maxX) to oy.coerceIn(minY, maxY)
    }

    // 本次显示模式对应的目标矩形。
    // 几何状态的初值直接取它：落位若只放在 LaunchedEffect 里（组合提交后才执行），
    // 首帧会先按初始 offset (0,0) 绘制，表现为播放器从左上角闪一下再跳到锚点位置。
    val floatingLayoutState = playerState.floatingPlayerLayoutState
    val targetRect = when (displayMode) {
        PlayerDisplayMode.FloatingLandscape -> {
            if (floatingLayoutState.initialized) {
                val width = with(density) { floatingLayoutState.widthPx.toDp() }
                val height = with(density) { floatingLayoutState.heightPx.toDp() }
                val (left, top) = clampFloatingOffset(
                    with(density) { floatingLayoutState.offsetXPx.toDp() },
                    with(density) { floatingLayoutState.offsetYPx.toDp() },
                    width,
                    height,
                )
                DpRect(left, top, left + width, top + height)
            } else {
                // 尚未初始化：先用内容区默认尺寸贴右上角
                val width = with(density) { baseBounds.width.toDp() }
                val height = with(density) { baseBounds.height.toDp() }
                val (left, top) = clampFloatingOffset(screenWidth - width, 0.dp, width, height)
                DpRect(left, top, left + width, top + height)
            }
        }
        else -> {
            val width = with(density) { baseBounds.width.toDp() }
            val height = with(density) { baseBounds.height.toDp() }
            val left = with(density) { baseBounds.left.toDp() }
            val top = with(density) { baseBounds.top.toDp() }
            DpRect(left, top, left + width, top + height)
        }
    }

    // 目标矩形的最新值：动画只插值进度、每帧读取这里的目标，
    // 这样动画期间页面滚动改变了落点，动画目标也能实时跟随
    val liveTargetRect = remember { mutableStateOf(targetRect) }
    SideEffect {
        if (liveTargetRect.value != targetRect) {
            liveTargetRect.value = targetRect
        }
    }

    var currentWidth by remember { mutableStateOf(targetRect.right - targetRect.left) }
    var currentHeight by remember { mutableStateOf(targetRect.bottom - targetRect.top) }
    val scope = rememberCoroutineScope()
    var offsetX by remember { mutableStateOf(targetRect.left) }
    var offsetY by remember { mutableStateOf(targetRect.top) }
    var isDragging by remember { mutableStateOf(false) }

    // 上次生效的显示模式：仅当模式真正切换时才播放几何滑行动画；
    // 初次组合或同模式内几何变化（窗口尺寸、封面位移等）直接落位，避免误动画
    var lastDisplayMode by remember { mutableStateOf(displayMode) }
    // 滑行动画进行中：期间不做直接落位，避免把动画跳到终点
    var isAnimating by remember { mutableStateOf(false) }

    // 立即落位到目标矩形（不播放动画）
    fun applyTargetRect() {
        val target = liveTargetRect.value
        currentWidth = target.right - target.left
        currentHeight = target.bottom - target.top
        offsetX = target.left
        offsetY = target.top
    }

    fun updateFloatingState() {
        playerState.updateFloatingPlayerLayoutState(
            playerState.floatingPlayerLayoutState.copy(
                widthPx = with(density) { currentWidth.toPx() },
                heightPx = with(density) { currentHeight.toPx() },
                offsetXPx = with(density) { offsetX.toPx() },
                offsetYPx = with(density) { offsetY.toPx() },
                initialized = true,
            )
        )
    }

    suspend fun animateToSafeArea() {
        val (targetX, targetY) = clampFloatingOffset(offsetX, offsetY, currentWidth, currentHeight)
        if (targetX == offsetX && targetY == offsetY) return
        val targetOffsetXPx = with(density) { targetX.toPx() }
        val targetOffsetYPx = with(density) { targetY.toPx() }
        val startOffsetXPx = with(density) { offsetX.toPx() }
        val startOffsetYPx = with(density) { offsetY.toPx() }
        coroutineScope {
            launch {
                animate(
                    initialValue = startOffsetXPx,
                    targetValue = targetOffsetXPx,
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
                    ),
                ) { value, _ ->
                    offsetX = with(density) { value.toDp() }
                }
            }
            launch {
                animate(
                    initialValue = startOffsetYPx,
                    targetValue = targetOffsetYPx,
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
                    ),
                ) { value, _ ->
                    offsetY = with(density) { value.toDp() }
                }
            }
        }
        updateFloatingState()
    }

    /**
     * 从当前几何滑行到目标矩形。
     *
     * 只对「动画进度」做插值，每帧重新读取最新目标：页面滚动会让落点不断移动，
     * 动画期间目标必须实时跟随，否则动画结束时会停在启动那一刻的旧目标上（表现为卡住）。
     */
    suspend fun animatePlayerRectTo() {
        val startLeftPx = with(density) { offsetX.toPx() }
        val startTopPx = with(density) { offsetY.toPx() }
        val startWidthPx = with(density) { currentWidth.toPx() }
        val startHeightPx = with(density) { currentHeight.toPx() }
        val startRect = DpRect(offsetX, offsetY, offsetX + currentWidth, offsetY + currentHeight)
        if (startRect == liveTargetRect.value) {
            return
        }
        val progress = Animatable(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 350,
                easing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
            ),
        ) {
            val target = liveTargetRect.value
            val targetLeftPx = with(density) { target.left.toPx() }
            val targetTopPx = with(density) { target.top.toPx() }
            val targetWidthPx = with(density) { (target.right - target.left).toPx() }
            val targetHeightPx = with(density) { (target.bottom - target.top).toPx() }
            offsetX = with(density) { lerp(startLeftPx, targetLeftPx, value).toDp() }
            offsetY = with(density) { lerp(startTopPx, targetTopPx, value).toDp() }
            currentWidth = with(density) { lerp(startWidthPx, targetWidthPx, value).toDp() }
            currentHeight = with(density) { lerp(startHeightPx, targetHeightPx, value).toDp() }
        }
        // 进度走完后可能仍有残差（动画期间目标又变了），补一次精确落位
        applyTargetRect()
    }

    /**
     * 把悬浮窗的目标几何回写到状态。
     *
     * 首次确定尺寸/位置、或位置被安全区 clamp 修正时写入，供持久化与下次切回悬浮窗使用；
     * 几何未变化时不写入，避免反复触发防抖持久化。
     */
    fun syncFloatingLayoutState() {
        val floatingState = playerState.floatingPlayerLayoutState
        val leftPx = with(density) { targetRect.left.toPx() }
        val topPx = with(density) { targetRect.top.toPx() }
        val widthPx = with(density) { (targetRect.right - targetRect.left).toPx() }
        val heightPx = with(density) { (targetRect.bottom - targetRect.top).toPx() }
        if (floatingState.widthPx == widthPx &&
            floatingState.heightPx == heightPx &&
            floatingState.offsetXPx == leftPx &&
            floatingState.offsetYPx == topPx
        ) {
            return
        }
        playerState.updateFloatingPlayerLayoutState(
            floatingState.copy(
                defaultWidthPx = widthPx,
                defaultHeightPx = heightPx,
                widthPx = widthPx,
                heightPx = heightPx,
                offsetXPx = leftPx,
                offsetYPx = topPx,
                initialized = true,
            )
        )
    }

    // 同一种显示模式内的几何跟随（页面滚动让落点移动、窗口尺寸变化等）：直接落位。
    // 放在 SideEffect 里同步执行，保证本帧测量/绘制前就用上最新几何；
    // 拖动与动画期间跳过——否则会在滑行动画中途把矩形跳到终点。
    SideEffect {
        if (isDragging || isAnimating || lastDisplayMode != displayMode) {
            return@SideEffect
        }
        applyTargetRect()
    }

    // 只有显示模式真正切换时才播放滑行动画。
    // key 只绑定 displayMode：落点随滚动变化、浮窗状态回写、竖屏布局状态更新都会让
    // 目标矩形变化，若把它们放进 key，动画刚起步就会被重启，重启后模式已相同而直接落位，
    // 表现就是「没有位移动画、瞬间跳到目标」。
    LaunchedEffect(displayMode) {
        val previousMode = lastDisplayMode
        lastDisplayMode = displayMode
        if (isDragging) {
            return@LaunchedEffect
        }
        // 画中画：小窗由系统瞬时创建/销毁，窗口尺寸同时跳变，几何直接落位；
        // 若照常播放滑行动画，小窗内会先出现上一个大窗口尺寸的裁切画面。
        // 首次组合（模式未变）同样直接落位
        if (previousMode == displayMode || displayMode == PlayerDisplayMode.PictureInPicture) {
            applyTargetRect()
            return@LaunchedEffect
        }
        // 模式切换：从旧模式矩形滑行到新模式目标矩形（如封面锚点 ↔ 悬浮窗、悬浮窗 ↔ 全屏）
        isAnimating = true
        try {
            animatePlayerRectTo()
        } finally {
            isAnimating = false
        }
    }

    // 悬浮窗几何回写：首次确定尺寸、越界被 clamp 修正、切回悬浮窗时同步到状态，供持久化
    LaunchedEffect(displayMode, targetRect, isDragging) {
        if (isDragging || displayMode != PlayerDisplayMode.FloatingLandscape) {
            return@LaunchedEffect
        }
        syncFloatingLayoutState()
    }

    val modifier = if (displayMode == PlayerDisplayMode.FloatingLandscape) {
        // 位置与尺寸已统一由外层 root Box 的 offset/size 承担（所有模式一致，可做切换动画），
        // 这里只挂悬浮窗的拖动/缩放手势
        Modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downPos = down.position

                    val bottomEdgePx = 12.dp.toPx()
                    val isBottomEdge = currentHeight.toPx() - downPos.y <= bottomEdgePx

                    val startOffsetX = offsetX
                    val startWidth = currentWidth
                    val startHeight = currentHeight
                    val startWidthPx = startWidth.toPx()
                    val startHeightPx = startHeight.toPx()
                    val aspectRatio = startWidthPx / startHeightPx

                    val pointerId = down.id
                    var dragging = false
                    isDragging = true

                    val minWidthPx = 200.dp.toPx()
                    val maxWidthPx = 800.dp.toPx()
                    val minHeightPx = 112.dp.toPx()
                    val maxHeightPx = 450.dp.toPx()

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                        if (!change.pressed) {
                            isDragging = false
                            if (dragging) {
                                scope.launch { animateToSafeArea() }
                            }
                            break
                        }
                        if (!change.positionChanged()) continue

                        val deltaX = change.position.x - downPos.x
                        val deltaY = change.position.y - downPos.y

                        if (!dragging) {
                            if (abs(deltaX) < 2f && abs(deltaY) < 2f) continue
                            dragging = true
                        }

                        if (isBottomEdge) {
                            val newHeightPx = (startHeightPx + deltaY).coerceIn(minHeightPx, maxHeightPx)
                            val newWidthPx = (newHeightPx * aspectRatio).coerceIn(minWidthPx, maxWidthPx)
                            val clampedHeightPx = newWidthPx / aspectRatio
                            val deltaWPx = newWidthPx - startWidthPx

                            currentWidth = newWidthPx.toDp()
                            currentHeight = clampedHeightPx.toDp()
                            val zoneWidth = startWidthPx / 3
                            offsetX = when {
                                downPos.x < zoneWidth -> startOffsetX - deltaWPx.toDp()
                                downPos.x < zoneWidth * 2 -> startOffsetX - (deltaWPx / 2).toDp()
                                else -> startOffsetX
                            }
                            updateFloatingState()
                            change.consume()
                        } else {
                            val pan = change.position - change.previousPosition
                            offsetX += pan.x.toDp()
                            offsetY += pan.y.toDp()
                            updateFloatingState()
                            change.consume()
                        }
                    }
                }
            }
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            // 播放器在 Scaffold 中的最终位置（所有模式统一由动画 offset 承担，
            // 使模式切换时可以平滑滑行；layout 层已把本层固定在 (0, 0)）
            .offset { IntOffset(offsetX.roundToPx(), offsetY.roundToPx()) }
            .then(
                if (baseBounds.width == Float.POSITIVE_INFINITY || baseBounds.height == Float.POSITIVE_INFINITY) {
                    Modifier.fillMaxSize()
                } else if (displayMode == PlayerDisplayMode.EmbeddedPortrait) {
                    Modifier
                        .background(Color.Black)
                        .padding(top = portraitTopInset)
                        .size(currentWidth, currentHeight)
                } else if (displayMode == PlayerDisplayMode.AnchorOverlay) {
                    Modifier
                        .background(Color.Black)
                        .size(currentWidth, currentHeight)
                } else if (displayMode == PlayerDisplayMode.PictureInPicture) {
                    // 画中画窗口内只有播放器画面：补黑底，避免画面未铺满时透出应用内容
                    Modifier
                        .background(Color.Black)
                        .size(currentWidth, currentHeight)
                } else {
                    Modifier.size(currentWidth, currentHeight)
                }
            )
            .then(modifier)
    ) {
        CompositionLocalProvider(
            LocalPlayerState provides playerState,
            LocalPlayerDisplayMode provides displayMode,
        ) {
            playerContent()
        }
    }
}
