package cn.a10miaomiao.bilimiao.compose.components.layout

import android.app.Activity
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import cn.a10miaomiao.bilimiao.compose.StartViewWrapper
import kotlin.math.abs
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBar
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarConfig
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarHorizontal
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarOrientation
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarState
import cn.a10miaomiao.bilimiao.compose.components.appbar.MenuItemData
import cn.a10miaomiao.bilimiao.compose.components.appbar.rememberAppBarState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    var playerWidth by remember { mutableStateOf(0.dp) }
    var playerHeight by remember { mutableStateOf(0.dp) }

    LaunchedEffect(showPlayer, fullScreenPlayer, orientation, smallModePlayerCurrentHeight, smallModePlayerMinHeight, playerSmallShowAreaWidth, playerSmallShowAreaHeight, playerVideoRatio) {
        val activity = context as Activity
        val densityValue = activity.resources.displayMetrics.density
        val screenWidth = activity.resources.displayMetrics.widthPixels
        val screenHeight = activity.resources.displayMetrics.heightPixels

        when {
            fullScreenPlayer -> {
                playerWidth = Dp.Infinity
                playerHeight = Dp.Infinity
            }
            showPlayer && orientation == 1 -> {
                val maxHeightByRatio = (screenWidth / playerVideoRatio).toInt()
                val maxHeight = minOf(maxHeightByRatio, screenHeight / 2)
                val minHeight = smallModePlayerMinHeight.toFloat().coerceAtLeast(200f * densityValue)
                val targetHeight = if (smallModePlayerCurrentHeight > 0) {
                    minOf(smallModePlayerCurrentHeight.toFloat(), maxHeight.toFloat())
                } else {
                    minHeight
                }
                playerWidth = (screenWidth / densityValue).dp
                playerHeight = (targetHeight / densityValue).dp
            }
            showPlayer && orientation == 2 -> {
                playerWidth = if (playerSmallShowAreaWidth > 0) {
                    (playerSmallShowAreaWidth / densityValue).dp
                } else {
                    480.dp
                }
                playerHeight = if (playerSmallShowAreaHeight > 0) {
                    (playerSmallShowAreaHeight / densityValue).dp
                } else {
                    270.dp
                }
            }
            else -> {
                playerWidth = 0.dp
                playerHeight = 0.dp
            }
        }
    }

    ModalNavigationDrawer(
        modifier = modifier.fillMaxSize(),
        drawerState = modalDrawerState,
        drawerContent = drawerContent,
        content = {
            // 主内容区
            Box(modifier = Modifier.fillMaxSize()) {
                // 根据是否有 AppBarState 决定布局
                if (appBarState != null && appBarState.visible) {
                    when (appBarState.orientation) {
                        AppBarOrientation.Vertical -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // 内容区域
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(top = if (showPlayer && orientation == 1) playerHeight else 0.dp)
                                        .then(
                                            if (appBarNestedScrollConnection != null) {
                                                Modifier.nestedScroll(appBarNestedScrollConnection)
                                            } else {
                                                Modifier
                                            }
                                        )
                                ) {
                                    content()
                                }

                                AnimatedVisibility(
                                    visible = appBarState.barVisible,
                                    enter = slideInVertically { it } + fadeIn(),
                                    exit = slideOutVertically { it } + fadeOut(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
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
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }

                        AppBarOrientation.Horizontal -> {
                            Row(modifier = Modifier.fillMaxSize()) {
                                // 横屏 AppBar
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
                                    modifier = Modifier.fillMaxHeight(),
                                )

                                // 右侧分隔线
                                VerticalDivider(
                                    modifier = Modifier.fillMaxHeight(),
                                    thickness = AppBarConfig.DividerHeight,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )

                                // 内容区域
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    content()
                                }
                            }
                        }
                    }
                } else {
                    // 无 AppBar 时的原始布局
                    Column {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = if (showPlayer && orientation == 1) playerHeight else 0.dp)
                        ) {
                            content()
                        }
                    }
                }

                // 播放器层
                if (showPlayer && playerWidth > 0.dp && playerHeight > 0.dp) {
                    PlayerLayer(
                        startViewWrapper = startViewWrapper,
                        width = playerWidth,
                        height = playerHeight,
                    )
                }
            }
        }
    )
}

@Composable
internal fun PlayerLayer(
    startViewWrapper: StartViewWrapper,
    width: Dp,
    height: Dp,
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
    var currentWidth by remember { mutableStateOf(width) }
    var currentHeight by remember { mutableStateOf(height) }
    var dragMode by remember { mutableStateOf("move") }
    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }

    val context = LocalContext.current
    val screenWidth = density.run { context.resources.displayMetrics.widthPixels.toDp() }
    val screenHeight = density.run { context.resources.displayMetrics.heightPixels.toDp() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(width, height) {
        currentWidth = width
        currentHeight = height
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
                if (currentWidth == Dp.Infinity || currentHeight == Dp.Infinity) {
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
