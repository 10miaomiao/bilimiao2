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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.assets.BilimiaoIcons
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.Player
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.player.Danmakuoff
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.player.Danmakuon
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo

@Composable
fun DesktopPlayerControls(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    playbackSpeed: Float,
    isFullscreen: Boolean = false,
    danmakuVisible: Boolean = true,
    volume: Int = 100,
    qualityList: List<PlayerSourceInfo.AcceptInfo> = emptyList(),
    currentQuality: Int = 0,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onFullscreen: () -> Unit,
    onDanmakuToggle: () -> Unit = {},
    onVolumeChange: (Int) -> Unit = {},
    onQualityChange: (Int) -> Unit = {},
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
        // 进度条
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
            // 播放/暂停
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    tint = Color.White,
                )
            }

            // 时间
            Text(
                text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
            )

            Spacer(Modifier.weight(1f))

            // 清晰度选择
            if (qualityList.size > 1) {
                QualitySelector(
                    qualityList = qualityList,
                    currentQuality = currentQuality,
                    onQualityChange = onQualityChange,
                )
            }

            // 倍速选择
            SpeedSelector(
                currentSpeed = playbackSpeed,
                onSpeedChange = onSpeedChange,
            )

            // 弹幕开关
            IconButton(onClick = onDanmakuToggle) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = if (danmakuVisible) BilimiaoIcons.Player.Danmakuoff else BilimiaoIcons.Player.Danmakuon,
                    contentDescription = if (danmakuVisible) "关闭弹幕" else "开启弹幕",
                    tint = if (danmakuVisible) Color.White else Color.White.copy(alpha = 0.5f),
                )
            }

            // 音量控制
            VolumeControl(
                volume = volume,
                onVolumeChange = onVolumeChange,
            )

            // 全屏
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
private fun QualitySelector(
    qualityList: List<PlayerSourceInfo.AcceptInfo>,
    currentQuality: Int,
    onQualityChange: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = qualityList.firstOrNull { it.quality == currentQuality }?.description ?: "${currentQuality}P"

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(currentLabel, color = Color.White, style = MaterialTheme.typography.bodySmall)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            qualityList.forEach { info ->
                DropdownMenuItem(
                    text = {
                        Text(
                            info.description,
                            color = if (info.quality == currentQuality) MaterialTheme.colorScheme.primary else Color.Unspecified,
                        )
                    },
                    onClick = {
                        onQualityChange(info.quality)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun VolumeControl(
    volume: Int,
    onVolumeChange: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var previousVolume by remember { mutableStateOf(volume) }

    Box {
        IconButton(onClick = {
            if (volume > 0) {
                previousVolume = volume
                onVolumeChange(0)
            } else {
                onVolumeChange(previousVolume.coerceAtLeast(50))
            }
        }) {
            Icon(
                imageVector = when {
                    volume == 0 -> Icons.Default.VolumeOff
                    volume < 50 -> Icons.Default.VolumeDown
                    else -> Icons.Default.VolumeUp
                },
                contentDescription = "音量",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            Slider(
                value = volume.toFloat(),
                onValueChange = { onVolumeChange(it.toInt()) },
                valueRange = 0f..100f,
                modifier = Modifier.width(120.dp).padding(horizontal = 8.dp),
            )
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

            drawLine(
                color = inactiveColor,
                start = Offset(thumbRadiusPx, centerY),
                end = Offset(barWidth - thumbRadiusPx, centerY),
                strokeWidth = trackHeight.toPx(),
                cap = StrokeCap.Round,
            )
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
            Text(
                "${currentSpeed}x",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            speeds.forEach { speed ->
                DropdownMenuItem(
                    text = {
                        Text(
                            "${speed}x",
                            color = if (speed == currentSpeed) MaterialTheme.colorScheme.primary else Color.Unspecified,
                        )
                    },
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
