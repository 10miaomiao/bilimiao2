package cn.a10miaomiao.bilimiao.compose.components.layout

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import cn.a10miaomiao.bilimiao.compose.StartViewWrapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ComposeScaffold(
    startViewWrapper: StartViewWrapper,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
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
                // 竖屏模式：根据视频比例计算播放器尺寸，限制最大高度为半屏
                // smallModePlayerMinHeight 和 smallModePlayerCurrentHeight 是像素值
                val maxHeightByRatio = (screenWidth / playerVideoRatio).toInt()
                val maxHeight = minOf(maxHeightByRatio, screenHeight / 2)
                val minHeight = smallModePlayerMinHeight.toFloat().coerceAtLeast(200f * densityValue)
                val targetHeight = if (smallModePlayerCurrentHeight > 0) {
                    minOf(smallModePlayerCurrentHeight.toFloat(), maxHeight.toFloat())
                } else {
                    minHeight
                }
                // 竖屏时宽度填满屏幕
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

    Box(modifier = modifier) {
        Column {
            // 内容区域，竖屏小窗播放时添加 top padding 避免被遮挡
            Box(
                modifier = Modifier.then(
                    if (showPlayer && orientation == 1 && playerHeight > 0.dp) {
                        Modifier.padding(top = playerHeight)
                    } else {
                        Modifier
                    }
                )
            ) {
                content()
            }
        }

        if (showPlayer && playerWidth > 0.dp && playerHeight > 0.dp) {
            PlayerLayer(
                startViewWrapper = startViewWrapper,
                width = playerWidth,
                height = playerHeight,
            )
        }
    }
}

@Composable
private fun PlayerLayer(
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
