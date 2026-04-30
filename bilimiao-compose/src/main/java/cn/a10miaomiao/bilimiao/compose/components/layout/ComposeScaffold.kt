package cn.a10miaomiao.bilimiao.compose.components.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
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
import cn.a10miaomiao.bilimiao.compose.common.ContentInsets
import cn.a10miaomiao.bilimiao.compose.common.LocalContentInsets
import cn.a10miaomiao.bilimiao.compose.common.toContentInsets
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBar
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarHorizontal
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarOrientation
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarState
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
                            windowInsets = rawWindowInsets,
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
    windowInsets: ContentInsets
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

    val context = LocalContext.current
    val screenWidth = density.run { context.resources.displayMetrics.widthPixels.toDp() }

    LaunchedEffect(baseBounds) {
        currentWidth = with(density) { baseBounds.width.toDp() }
        currentHeight = with(density) { baseBounds.height.toDp() }
        if (orientation != 2) {
            offsetX = 0.dp
            offsetY = 0.dp
        } else if (offsetX == 0.dp && offsetY == 0.dp) {
            // Initialize landscape default position to top-right (matching native RT default)
            offsetX = screenWidth - currentWidth
            offsetY = 0.dp
        }
    }

    val modifier = if (orientation == 2 && !startViewWrapper.fullScreenPlayer) {
        Modifier
            .offset(x = offsetX, y = offsetY)
            .size(currentWidth, currentHeight)
            .pointerInput(Unit) {
                detectTransformGestures(panZoomLock = true) { centroid, pan, zoom, rotation ->
                    if (zoom != 1f) {
                        // Pinch to resize
                        val newWidth = (currentWidth.value * zoom).coerceIn(200f, 800f).dp
                        val newHeight = (currentHeight.value * zoom).coerceIn(112f, 450f).dp
                        currentWidth = newWidth
                        currentHeight = newHeight
                    } else {
                        // Single finger drag to move
                        offsetX += pan.x.toDp()
                        offsetY += pan.y.toDp()
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
                } else if (orientation == 1){
                    Modifier
                        .background(Color.Black)
                        .padding(
                            top = windowInsets.top,
                        )
                        .size(currentWidth, currentHeight)
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
