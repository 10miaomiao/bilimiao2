package cn.a10miaomiao.bilimiao.compose.components.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import cn.a10miaomiao.bilimiao.compose.StartViewWrapper
import cn.a10miaomiao.bilimiao.compose.common.LocalContentInsets
import cn.a10miaomiao.bilimiao.compose.common.toContentInsets
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBar
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarHorizontal
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarOrientation
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun ComposeScaffold(
    startViewWrapper: StartViewWrapper,
    modifier: Modifier = Modifier,
    appBarState: AppBarState? = null,
    drawerContent: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val context = LocalContext.current

    val showPlayer = startViewWrapper.showPlayer
    val fullScreenPlayer = startViewWrapper.fullScreenPlayer
    val orientation = startViewWrapper.orientation
    val smallModePlayerCurrentHeight = startViewWrapper.smallModePlayerCurrentHeight
    val smallModePlayerMinHeight = startViewWrapper.smallModePlayerMinHeight
    val playerSmallShowAreaWidth = startViewWrapper.playerSmallShowAreaWidth
    val playerSmallShowAreaHeight = startViewWrapper.playerSmallShowAreaHeight
    val playerVideoRatio = startViewWrapper.playerVideoRatio
    val drawerState = startViewWrapper.drawerState

    val modalDrawerState = rememberDrawerState(
        initialValue = if (drawerState == 3) DrawerValue.Open else DrawerValue.Closed
    )
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

    LaunchedEffect(drawerState) {
        when (drawerState) {
            3 -> modalDrawerState.open()
            4 -> modalDrawerState.close()
        }
    }

    LaunchedEffect(modalDrawerState.currentValue) {
        when (modalDrawerState.currentValue) {
            DrawerValue.Open -> startViewWrapper.setDrawerState(3)
            DrawerValue.Closed -> startViewWrapper.setDrawerState(4)
        }
    }

    LaunchedEffect(appBarState?.orientation) {
        if (appBarState?.orientation == AppBarOrientation.Horizontal) {
            appBarState.showBar()
            appBarState.showMenu()
        }
    }

    val rawWindowInsets = WindowInsets.safeDrawing.toContentInsets()
    val playerLayoutState = remember(
        showPlayer,
        fullScreenPlayer,
        orientation,
        smallModePlayerCurrentHeight,
        smallModePlayerMinHeight,
        playerSmallShowAreaWidth,
        playerSmallShowAreaHeight,
        playerVideoRatio,
    ) {
        ComposeScaffoldPlayerLayoutState(
            showPlayer = showPlayer,
            fullScreenPlayer = fullScreenPlayer,
            orientation = orientation,
            smallModePlayerCurrentHeight = smallModePlayerCurrentHeight,
            smallModePlayerMinHeight = smallModePlayerMinHeight,
            playerSmallShowAreaWidth = playerSmallShowAreaWidth,
            playerSmallShowAreaHeight = playerSmallShowAreaHeight,
            playerVideoRatio = playerVideoRatio,
        )
    }

    ModalNavigationDrawer(
        modifier = modifier.fillMaxSize(),
        drawerState = modalDrawerState,
        drawerContent = drawerContent,
        content = {
            SubcomposeLayout(modifier = Modifier.fillMaxSize()) { constraints ->
                val layoutResult = calculateComposeScaffoldLayout(
                    viewportWidth = with(this) { constraints.maxWidth.toDp() },
                    viewportHeight = with(this) { constraints.maxHeight.toDp() },
                    rawWindowInsets = rawWindowInsets,
                    appBarState = appBarState,
                    playerState = playerLayoutState,
                )

                val contentPlaceable = subcompose("content-${layoutResult.contentInsets}") {
                    CompositionLocalProvider(
                        LocalContentInsets provides layoutResult.contentInsets,
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
                                themeColor = appBarState.themeColor,
                                backgroundColor = appBarState.backgroundColor,
                                menuExpanded = appBarState.menuExpanded,
                                appBarState = appBarState,
                                onBackClick = { appBarState._onBackClick?.invoke() },
                                onMenuClick = { appBarState._onMenuClick?.invoke() },
                                onMenuItemClick = { appBarState._onMenuItemClick?.invoke(it) },
                                onPointerClick = { appBarState._onPointerClick?.invoke() },
                                onExchangeClick = { appBarState._onExchangeClick?.invoke() },
                                modifier = Modifier.fillMaxSize(),
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
                            themeColor = appBarState.themeColor,
                            backgroundColor = appBarState.backgroundColor,
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
                        )
                    }.single().measure(boundsConstraints(layoutResult.playerBounds!!))
                } else {
                    null
                }

                layout(constraints.maxWidth, constraints.maxHeight) {
                    val contentRect = layoutResult.contentBounds
                    contentPlaceable.placeRelative(contentRect.left.toInt(), contentRect.top.toInt())

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
        }
    )
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
) {
    val playerView = startViewWrapper.playerView
    val completionView = startViewWrapper.completionView
    val errorMessageView = startViewWrapper.errorMessageView
    val areaLimitView = startViewWrapper.areaLimitView
    val loadingView = startViewWrapper.loadingView
    val orientation = startViewWrapper.orientation
    val density = LocalDensity.current

    if (playerView == null) {
        return
    }

    var offsetX by remember { mutableStateOf(0.dp) }
    var offsetY by remember { mutableStateOf(0.dp) }
    var currentWidth by remember { mutableStateOf(with(density) { baseBounds.width.toDp() }) }
    var currentHeight by remember { mutableStateOf(with(density) { baseBounds.height.toDp() }) }
    var dragMode by remember { mutableStateOf("move") }
    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }

    val context = LocalContext.current
    val screenWidth = density.run { context.resources.displayMetrics.widthPixels.toDp() }
    val screenHeight = density.run { context.resources.displayMetrics.heightPixels.toDp() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(baseBounds) {
        currentWidth = with(density) { baseBounds.width.toDp() }
        currentHeight = with(density) { baseBounds.height.toDp() }
        if (orientation != 2) {
            offsetX = 0.dp
            offsetY = 0.dp
        }
    }

    val edgeThreshold = 48.dp

    val modifier = if (orientation == 2) {
        Modifier
            .offset(x = offsetX, y = offsetY)
            .size(currentWidth, currentHeight)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        velocityX = 0f
                        velocityY = 0f
                        val boxSize = this.size
                        val isBottom = offset.y > boxSize.height - edgeThreshold.toPx()
                        val isLeft = offset.x < edgeThreshold.toPx()
                        val isRight = offset.x > boxSize.width - edgeThreshold.toPx()

                        dragMode = when {
                            isBottom && isLeft -> "resize_left"
                            isBottom && isRight -> "resize_right"
                            isBottom -> "resize_bottom"
                            else -> "move"
                        }
                    },
                    onDrag = { change, dragAmount ->
                        velocityX = change.position.x - change.previousPosition.x
                        velocityY = change.position.y - change.previousPosition.y
                        change.consume()
                        when (dragMode) {
                            "resize_right" -> {
                                val newWidth = (currentWidth.value + dragAmount.x / density.density).dp
                                val newHeight = (currentHeight.value + dragAmount.y / density.density).dp
                                if (newWidth.value in 200f..800f) {
                                    currentWidth = newWidth
                                }
                                if (newHeight.value in 112f..450f) {
                                    currentHeight = newHeight
                                }
                            }
                            "resize_left" -> {
                                val deltaX = dragAmount.x / density.density
                                val deltaY = dragAmount.y / density.density
                                val newWidth = (currentWidth.value - deltaX).dp
                                val newHeight = (currentHeight.value + deltaY).dp
                                if (newWidth.value in 200f..800f) {
                                    currentWidth = newWidth
                                    offsetX += deltaX.toDp()
                                }
                                if (newHeight.value in 112f..450f) {
                                    currentHeight = newHeight
                                }
                            }
                            "resize_bottom" -> {
                                val deltaY = dragAmount.y / density.density
                                val newHeight = (currentHeight.value + deltaY).dp
                                val ratio = currentWidth.value / currentHeight.value
                                if (newHeight.value in 112f..450f) {
                                    currentHeight = newHeight
                                    currentWidth = (newHeight.value * ratio).coerceIn(200f..800f).dp
                                }
                            }
                            "move" -> {
                                offsetX += dragAmount.x.toDp()
                                offsetY += dragAmount.y.toDp()
                            }
                        }
                    },
                    onDragEnd = {
                        if (dragMode == "move") {
                            coroutineScope.launch {
                                var vx = kotlin.math.abs(velocityX) * 0.5f
                                var vy = kotlin.math.abs(velocityY) * 0.5f
                                val friction = 0.92f
                                var ox = offsetX.value
                                var oy = offsetY.value
                                val maxX = screenWidth.value - currentWidth.value
                                val maxY = screenHeight.value - currentHeight.value

                                while (vx > 0.5f || vy > 0.5f) {
                                    vx *= friction
                                    vy *= friction
                                    val dirX = if (ox < 0) -1 else if (ox > maxX) -1 else 1
                                    val dirY = if (oy < 0) -1 else if (oy > maxY) -1 else 1
                                    ox += vx * dirX
                                    oy += vy * dirY

                                    ox = ox.coerceIn(0f, maxX)
                                    oy = oy.coerceIn(0f, maxY)

                                    offsetX = ox.dp
                                    offsetY = oy.dp
                                    delay(16)
                                }

                                offsetX = ox.coerceIn(0f, maxX).dp
                                offsetY = oy.coerceIn(0f, maxY).dp
                                velocityX = 0f
                                velocityY = 0f
                            }
                        }
                    }
                )
            }
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .then(
                if (baseBounds.width == Float.POSITIVE_INFINITY || baseBounds.height == Float.POSITIVE_INFINITY) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier.size(currentWidth, currentHeight)
                }
            )
            .then(modifier)
    ) {
        AndroidView(
            factory = { ctx ->
                playerView
            },
            onReset = { view ->
//                (view.parent as ViewGroup).removeView(view)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
