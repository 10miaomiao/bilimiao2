package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.DesktopPlayerDelegate
import kotlinx.coroutines.delay
import org.kodein.di.compose.rememberInstance

@Composable
fun DesktopPlayerContainer(
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false,
    onFullscreenToggle: () -> Unit = {},
) {
    val basePlayerDelegate by rememberInstance<BasePlayerDelegate>()
    val playerDelegate = basePlayerDelegate as DesktopPlayerDelegate
    val player = playerDelegate.mediampPlayer
    val isPlaying by playerDelegate.isPlayingState.collectAsState()
    val currentPosition by playerDelegate.currentPosition.collectAsState()
    val playbackSpeed by playerDelegate.playbackSpeed.collectAsState()
    val duration by playerDelegate.duration.collectAsState()
    val isLoading by playerDelegate.isLoading.collectAsState()
    val loadingMessage by playerDelegate.loadingMessage.collectAsState()
    val errorMessage by playerDelegate.errorMessage.collectAsState()
    val danmakuParser by playerDelegate.danmakuParser.collectAsState()
    val isCompleted by playerDelegate.isCompleted.collectAsState()
    val danmakuVisible by playerDelegate.danmakuVisible.collectAsState()
    val volume by playerDelegate.volume.collectAsState()
    val playerSourceInfo by playerDelegate.playerSourceInfo.collectAsState()
    val currentQuality by playerDelegate.currentQuality.collectAsState()

    val focusRequester = remember { FocusRequester() }

    // 自动隐藏控制栏
    var controlsVisible by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (isPlaying && controlsVisible && System.currentTimeMillis() - lastInteractionTime > 3000) {
                controlsVisible = false
            }
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.Spacebar -> {
                            if (isPlaying) playerDelegate.pause() else playerDelegate.resume()
                            controlsVisible = true
                            lastInteractionTime = System.currentTimeMillis()
                            true
                        }
                        Key.F -> {
                            onFullscreenToggle()
                            true
                        }
                        Key.Escape -> {
                            if (isFullscreen) {
                                onFullscreenToggle()
                                true
                            } else false
                        }
                        Key.DirectionLeft -> {
                            playerDelegate.seekTo((currentPosition - 5000).coerceAtLeast(0))
                            controlsVisible = true
                            lastInteractionTime = System.currentTimeMillis()
                            true
                        }
                        Key.DirectionRight -> {
                            playerDelegate.seekTo((currentPosition + 5000).coerceAtMost(duration))
                            controlsVisible = true
                            lastInteractionTime = System.currentTimeMillis()
                            true
                        }
                        Key.DirectionUp -> {
                            playerDelegate.setVolume(volume + 5)
                            controlsVisible = true
                            lastInteractionTime = System.currentTimeMillis()
                            true
                        }
                        Key.DirectionDown -> {
                            playerDelegate.setVolume(volume - 5)
                            controlsVisible = true
                            lastInteractionTime = System.currentTimeMillis()
                            true
                        }
                        Key.M -> {
                            playerDelegate.toggleDanmaku()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Move) {
                            controlsVisible = true
                            lastInteractionTime = System.currentTimeMillis()
                        }
                    }
                }
            }
            .clickable {
                focusRequester.requestFocus()
            }
    ) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            lastInteractionTime = System.currentTimeMillis()
        }

        player?.let { p ->
            // 视频画面 + 鼠标手势
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (isPlaying) playerDelegate.pause() else playerDelegate.resume()
                                controlsVisible = true
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            onDoubleTap = {
                                onFullscreenToggle()
                            },
                        )
                    }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == PointerEventType.Scroll) {
                                    val scrollY = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                                    if (scrollY != 0f) {
                                        playerDelegate.setVolume(volume + if (scrollY < 0) 5 else -5)
                                    }
                                }
                            }
                        }
                    },
            ) {
                // 视频渲染
                DesktopPlayerSurface(
                    player = p,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // 弹幕覆盖层
            DanmakuOverlay(
                currentPosition = currentPosition,
                isPlaying = isPlaying,
                playbackSpeed = playbackSpeed,
                danmakuParser = danmakuParser,
                visible = danmakuVisible,
                modifier = Modifier.fillMaxSize(),
            )

            // 顶栏
            AnimatedVisibility(visible = controlsVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                            )
                        )
                        .align(Alignment.TopStart),
                ) {
                    IconButton(onClick = { playerDelegate.closePlayer() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color.White,
                        )
                    }
                }
            }

            // 加载指示器
            if (isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = Color.White)
                    if (loadingMessage.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = loadingMessage,
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            // 播放完成覆盖层
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = "播放完成",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            OutlinedButton(onClick = { playerDelegate.replay() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(4.dp))
                                Text("重播", color = Color.White)
                            }
                            val hasNext = playerDelegate.currentSource.value?.next() != null
                            if (hasNext) {
                                Button(onClick = { playerDelegate.playNext() }) {
                                    Icon(Icons.Default.SkipNext, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("下一个")
                                }
                            }
                            OutlinedButton(onClick = { playerDelegate.closePlayer() }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(4.dp))
                                Text("关闭", color = Color.White)
                            }
                        }
                    }
                }
            }

            // 错误信息 + 重试
            errorMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = msg,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Button(onClick = { playerDelegate.retry() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("重试")
                            }
                            OutlinedButton(onClick = { playerDelegate.closePlayer() }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(4.dp))
                                Text("关闭", color = Color.White)
                            }
                        }
                    }
                }
            }

            // 底部控制栏
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
                visible = controlsVisible
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                        .align(Alignment.BottomCenter),
                ) {
                    DesktopPlayerControls(
                        isPlaying = isPlaying,
                        currentPosition = currentPosition,
                        duration = duration,
                        playbackSpeed = 1.0f,
                        isFullscreen = isFullscreen,
                        danmakuVisible = danmakuVisible,
                        volume = volume,
                        qualityList = playerSourceInfo?.acceptList ?: emptyList(),
                        currentQuality = currentQuality,
                        onPlayPause = {
                            if (isPlaying) playerDelegate.pause() else playerDelegate.resume()
                        },
                        onSeek = { playerDelegate.seekTo(it) },
                        onSpeedChange = { playerDelegate.setPlaybackSpeed(it) },
                        onFullscreen = onFullscreenToggle,
                        onDanmakuToggle = { playerDelegate.toggleDanmaku() },
                        onVolumeChange = { playerDelegate.setVolume(it) },
                        onQualityChange = { playerDelegate.changeQuality(it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (visible) {
        Box(modifier) {
            content()
        }
    }
}
