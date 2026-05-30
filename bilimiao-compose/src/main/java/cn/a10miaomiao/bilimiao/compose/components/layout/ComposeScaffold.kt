package cn.a10miaomiao.bilimiao.compose.components.layout

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import cn.a10miaomiao.bilimiao.compose.StartViewWrapper
import cn.a10miaomiao.bilimiao.compose.common.LocalContentInsets
import cn.a10miaomiao.bilimiao.compose.common.ContentInsets
import cn.a10miaomiao.bilimiao.compose.common.toContentInsets
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBar
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarHorizontal
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarOrientation
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarState
import com.a10miaomiao.bilimiao.comm.utils.MiaoLogger
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

@Composable
fun ComposeScaffold(
    startViewWrapper: StartViewWrapper,
    modifier: Modifier = Modifier,
    appBarState: AppBarState? = null,
    allowDrawerOpenGesture: Boolean = true,
    drawerContent: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val showPlayer = startViewWrapper.showPlayer
    val fullScreenPlayer = startViewWrapper.fullScreenPlayer
    val orientation = startViewWrapper.orientation
    val portraitPlayerLayoutState = startViewWrapper.portraitPlayerLayoutState
    val floatingPlayerLayoutState = startViewWrapper.floatingPlayerLayoutState
    val playerVideoRatio = startViewWrapper.playerVideoRatio
    val drawerState = startViewWrapper.drawerState

    val appBarNestedScrollConnection = remember(appBarState, orientation) {
        if (appBarState == null) {
            null
        } else {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: androidx.compose.ui.geometry.Offset,
                    available: androidx.compose.ui.geometry.Offset,
                    source: NestedScrollSource,
                ): androidx.compose.ui.geometry.Offset {
                    if (appBarState.orientation != AppBarOrientation.Vertical) {
                        appBarState.showBar()
                        appBarState.showMenu()
                        return androidx.compose.ui.geometry.Offset.Zero
                    }
                    if (source != NestedScrollSource.UserInput) {
                        return androidx.compose.ui.geometry.Offset.Zero
                    }
                    if (abs(consumed.y) < 0.5f) {
                        return androidx.compose.ui.geometry.Offset.Zero
                    }
                    if (consumed.y < 0) {
                        appBarState.hideMenu()
                        appBarState.hideBar()
                    } else if (consumed.y > 0) {
                        appBarState.showBar()
                        appBarState.showMenu()
                    }
                    return androidx.compose.ui.geometry.Offset.Zero
                }
            }
        }
    }

    LaunchedEffect(appBarState?.orientation) {
        if (appBarState?.orientation == AppBarOrientation.Horizontal) {
            appBarState.showBar()
            appBarState.showMenu()
        }
    }

    val rawWindowInsets = WindowInsets.safeDrawing.toContentInsets()
    val density = LocalDensity.current
    val playerLayoutState = remember(
        showPlayer,
        fullScreenPlayer,
        orientation,
        portraitPlayerLayoutState,
        floatingPlayerLayoutState,
        playerVideoRatio,
    ) {
        ComposeScaffoldPlayerLayoutState(
            showPlayer = showPlayer,
            fullScreenPlayer = fullScreenPlayer,
            orientation = orientation,
            portraitState = portraitPlayerLayoutState,
            floatingState = floatingPlayerLayoutState,
            playerVideoRatio = playerVideoRatio,
        )
    }
    val drawerController = rememberComposeDrawerController(
        initialState = drawerState,
        onStateChanged = startViewWrapper::setDrawerState,
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
            StartViewWrapper.DRAWER_STATE_EXPANDED -> drawerController.open()
            StartViewWrapper.DRAWER_STATE_COLLAPSED -> drawerController.close()
        }
    }

    LaunchedEffect(
        drawerController.openFraction,
        drawerController.currentValue,
        drawerController.targetValue,
    ) {
        drawerController.syncWrapperState()
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
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
                .pointerInput(startViewWrapper) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        startViewWrapper.setTouchStartTop(down.position.y)
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
                        startViewWrapper.setTouchStartTop(down.position.y)
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
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
//                                .then(
//                                    if (appBarNestedScrollConnection != null) {
//                                        Modifier.nestedScroll(appBarNestedScrollConnection)
//                                    } else {
//                                        Modifier
//                                    }
//                                )
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            content()
                        }
                    }
                }.single().measure(contentConstraints(constraints, layoutResult.contentBounds))

                val verticalAppBarPlaceable = if (layoutResult.hasVerticalAppBar && appBarState != null) {
                    subcompose("appBarVertical") {
                        AnimatedVisibility(
                            visible = appBarState.barVisible,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut(),
                        ) {
                            AppBar(
                                title = appBarState.title,
                                canBack = appBarState.canBack,
                                showPointer = appBarState.showPointer,
                                pointerOrientation = appBarState.pointerOrientation,
                                showExchange = appBarState.showExchange,
                                menus = appBarState.menus,
                                isNavigationMenu = appBarState.isNavigationMenu,
                                checkedKey = appBarState.checkedKey,
                                menuExpanded = appBarState.menuExpanded,
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
                        }
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

                val playerPlaceable = if (layoutResult.hasPlayer) {
                    subcompose("player") {
                        PlayerLayer(
                            startViewWrapper = startViewWrapper,
                            baseBounds = layoutResult.playerBounds!!,
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
                    layoutResult.playerBounds?.let { rect ->
                        playerPlaceable?.placeRelative(rect.left.toInt(), rect.top.toInt())
                    }
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
        dispatchState(StartViewWrapper.DRAWER_STATE_EXPANDED)
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
        dispatchState(StartViewWrapper.DRAWER_STATE_COLLAPSED)
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
        dispatchState(StartViewWrapper.DRAWER_STATE_SETTLING)
        state.animateTo(
            target,
            animationSpec = tween(durationMillis = DrawerSettleDurationMillis),
        )
        programmaticChange = false
        dispatchState(
            if (target == ComposeDrawerValue.Open) {
                StartViewWrapper.DRAWER_STATE_EXPANDED
            } else {
                StartViewWrapper.DRAWER_STATE_COLLAPSED
            }
        )
    }

    fun syncWrapperState() {
        if (programmaticChange) {
            return
        }
        when {
            isAnimationRunning -> {
                dispatchState(StartViewWrapper.DRAWER_STATE_SETTLING)
            }
            settledValue == ComposeDrawerValue.Open && targetValue == ComposeDrawerValue.Open -> {
                dispatchState(StartViewWrapper.DRAWER_STATE_EXPANDED)
            }
            settledValue == ComposeDrawerValue.Closed && targetValue == ComposeDrawerValue.Closed -> {
                dispatchState(StartViewWrapper.DRAWER_STATE_COLLAPSED)
            }
            else -> {
                dispatchState(StartViewWrapper.DRAWER_STATE_DRAGGING)
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
    return if (this == StartViewWrapper.DRAWER_STATE_EXPANDED) {
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
    startViewWrapper: StartViewWrapper,
    baseBounds: Rect,
    portraitTopInset: Dp,
    safeDrawingInsets: ContentInsets = ContentInsets(),
    viewportWidth: Dp = 0.dp,
    viewportHeight: Dp = 0.dp,
) {
    val playerView = startViewWrapper.playerView
    val completionView = startViewWrapper.completionView
    val errorMessageView = startViewWrapper.errorMessageView
    val areaLimitView = startViewWrapper.areaLimitView
    val loadingView = startViewWrapper.loadingView
    val orientation = startViewWrapper.orientation
    val density = LocalDensity.current
    val context = LocalContext.current

    if (playerView == null) {
        return
    }

    var currentWidth by remember { mutableStateOf(with(density) { baseBounds.width.toDp() }) }
    var currentHeight by remember { mutableStateOf(with(density) { baseBounds.height.toDp() }) }
    val scope = rememberCoroutineScope()
    var offsetX by remember { mutableStateOf(0.dp) }
    var offsetY by remember { mutableStateOf(0.dp) }
    var isDragging by remember { mutableStateOf(false) }

    val displayMode = when {
        !startViewWrapper.showPlayer -> PlayerDisplayMode.Hidden
        startViewWrapper.fullScreenPlayer -> PlayerDisplayMode.Fullscreen
        orientation == Configuration.ORIENTATION_PORTRAIT -> PlayerDisplayMode.EmbeddedPortrait
        orientation == Configuration.ORIENTATION_LANDSCAPE -> PlayerDisplayMode.FloatingLandscape
        else -> PlayerDisplayMode.Hidden
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

    fun updateFloatingState() {
        startViewWrapper.updateFloatingPlayerLayoutState(
            startViewWrapper.floatingPlayerLayoutState.copy(
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

    LaunchedEffect(baseBounds, displayMode, startViewWrapper.portraitPlayerLayoutState, if (isDragging) null else startViewWrapper.floatingPlayerLayoutState) {
        val defaultWidth = with(density) { baseBounds.width.toDp() }
        val defaultHeight = with(density) { baseBounds.height.toDp() }
        when (displayMode) {
            PlayerDisplayMode.Hidden,
            PlayerDisplayMode.Fullscreen,
            PlayerDisplayMode.EmbeddedPortrait,
            -> {
                currentWidth = defaultWidth
                currentHeight = defaultHeight
                offsetX = 0.dp
                offsetY = 0.dp
            }
            PlayerDisplayMode.FloatingLandscape -> {
                val floatingState = startViewWrapper.floatingPlayerLayoutState
                if (floatingState.initialized) {
                    currentWidth = with(density) { floatingState.widthPx.toDp() }
                    currentHeight = with(density) { floatingState.heightPx.toDp() }
                    val (clampedX, clampedY) = clampFloatingOffset(
                        with(density) { floatingState.offsetXPx.toDp() },
                        with(density) { floatingState.offsetYPx.toDp() },
                        currentWidth,
                        currentHeight,
                    )
                    offsetX = clampedX
                    offsetY = clampedY
                    if (with(density) { clampedX.toPx() } != floatingState.offsetXPx || with(density) { clampedY.toPx() } != floatingState.offsetYPx) {
                        startViewWrapper.updateFloatingPlayerLayoutState(
                            floatingState.copy(
                                offsetXPx = with(density) { clampedX.toPx() },
                                offsetYPx = with(density) { clampedY.toPx() },
                            )
                        )
                    }
                } else {
                    currentWidth = defaultWidth
                    currentHeight = defaultHeight
                    val (initX, initY) = clampFloatingOffset(
                        screenWidth - currentWidth,
                        0.dp,
                        currentWidth,
                        currentHeight,
                    )
                    offsetX = initX
                    offsetY = initY
                    startViewWrapper.updateFloatingPlayerLayoutState(
                        floatingState.copy(
                            defaultWidthPx = with(density) { currentWidth.toPx() },
                            defaultHeightPx = with(density) { currentHeight.toPx() },
                            widthPx = with(density) { currentWidth.toPx() },
                            heightPx = with(density) { currentHeight.toPx() },
                            offsetXPx = with(density) { offsetX.toPx() },
                            offsetYPx = with(density) { offsetY.toPx() },
                            initialized = true,
                        )
                    )
                }
            }
        }
    }

    val modifier = if (displayMode == PlayerDisplayMode.FloatingLandscape) {
        Modifier
            .offset(x = offsetX, y = offsetY)
            .size(currentWidth, currentHeight)
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
            .then(
                if (baseBounds.width == Float.POSITIVE_INFINITY || baseBounds.height == Float.POSITIVE_INFINITY) {
                    Modifier.fillMaxSize()
                } else if (displayMode == PlayerDisplayMode.EmbeddedPortrait) {
                    Modifier
                        .background(Color.Black)
                        .padding(top = portraitTopInset)
                        .size(currentWidth, currentHeight)
                } else {
                    Modifier.size(currentWidth, currentHeight)
                }
            )
            .then(modifier)
    ) {
        AndroidView(
            factory = { playerView },
            onReset = { _ ->
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
