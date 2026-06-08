package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun DesktopPlayerControls(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    playbackSpeed: Float,
    isFullscreen: Boolean = false,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableStateOf(0f) }

    val progress = if (isSeeking) seekPosition else {
        if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
    }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // 进度条（独立一行，更醒目）
        VideoSeekBar(
            progress = progress,
            onSeekStart = { isSeeking = true },
            onSeekChange = { seekPosition = it },
            onSeekEnd = {
                isSeeking = false
                onSeek((seekPosition * duration).toLong())
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        )

        // 控制栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 播放/暂停按钮
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    tint = Color.White,
                )
            }

            // 当前时间 / 总时长
            Text(
                text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
            )

            Spacer(Modifier.weight(1f))

            // 倍速选择
            SpeedSelector(
                currentSpeed = playbackSpeed,
                onSpeedChange = onSpeedChange,
            )

            // 全屏按钮
            IconButton(onClick = onFullscreen) {
                Icon(
                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = if (isFullscreen) "退出全屏" else "全屏",
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun VideoSeekBar(
    progress: Float,
    onSeekStart: () -> Unit,
    onSeekChange: (Float) -> Unit,
    onSeekEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isExpanded = isHovered || isDragged

    val trackHeight by animateDpAsState(if (isExpanded) 6.dp else 3.dp)
    val thumbRadiusDp by animateDpAsState(if (isExpanded) 7.dp else 0.dp)
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = Color.White.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .height(20.dp)
            .hoverable(interactionSource, enabled = true),
        contentAlignment = Alignment.CenterStart,
    ) {
        val density = LocalDensity.current
        val thumbRadiusPx = with(density) { thumbRadiusDp.toPx() }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxOf(trackHeight, thumbRadiusDp * 2))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeekStart()
                        onSeekChange(newProgress)
                        onSeekEnd()
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeekStart()
                            onSeekChange(newProgress)
                        },
                        onDrag = { change, _ ->
                            val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                            onSeekChange(newProgress)
                            change.consume()
                        },
                        onDragEnd = { onSeekEnd() },
                        onDragCancel = { onSeekEnd() },
                    )
                },
        ) {
            val centerY = size.height / 2
            val barWidth = size.width

            // 背景轨道
            drawLine(
                color = inactiveColor,
                start = Offset(thumbRadiusPx, centerY),
                end = Offset(barWidth - thumbRadiusPx, centerY),
                strokeWidth = trackHeight.toPx(),
                cap = StrokeCap.Round,
            )
            // 已播放进度
            val trackLeft = thumbRadiusPx
            val trackRight = barWidth - thumbRadiusPx
            val trackWidth = trackRight - trackLeft
            drawLine(
                color = activeColor,
                start = Offset(trackLeft, centerY),
                end = Offset(trackLeft + trackWidth * progress, centerY),
                strokeWidth = trackHeight.toPx(),
                cap = StrokeCap.Round,
            )
            // 拖拽圆点
            if (thumbRadiusPx > 0f) {
                drawCircle(
                    color = activeColor,
                    radius = thumbRadiusPx,
                    center = Offset(trackLeft + trackWidth * progress, centerY),
                )
            }
        }
    }
}

@Composable
private fun SpeedSelector(
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    Box {
        TextButton(onClick = { expanded = true }) {
            Text("${currentSpeed}x")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            speeds.forEach { speed ->
                DropdownMenuItem(
                    text = { Text("${speed}x") },
                    onClick = {
                        onSpeedChange(speed)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes % 60, seconds % 60)
    } else {
        "%02d:%02d".format(minutes, seconds % 60)
    }
}
