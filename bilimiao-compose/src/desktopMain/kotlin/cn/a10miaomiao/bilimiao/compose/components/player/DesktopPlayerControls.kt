package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DesktopPlayerControls(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableStateOf(0f) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 播放/暂停按钮
        IconButton(onClick = onPlayPause) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "暂停" else "播放"
            )
        }

        // 当前时间
        Text(
            text = formatTime(currentPosition),
            style = MaterialTheme.typography.bodySmall,
        )

        // 进度条
        Slider(
            value = if (isSeeking) seekPosition else {
                if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
            },
            onValueChange = { value ->
                isSeeking = true
                seekPosition = value
            },
            onValueChangeFinished = {
                isSeeking = false
                onSeek((seekPosition * duration).toLong())
            },
            modifier = Modifier.weight(1f),
        )

        // 总时长
        Text(
            text = formatTime(duration),
            style = MaterialTheme.typography.bodySmall,
        )

        // 倍速选择
        SpeedSelector(
            currentSpeed = playbackSpeed,
            onSpeedChange = onSpeedChange,
        )

        // 全屏按钮
        IconButton(onClick = onFullscreen) {
            Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = "全屏"
            )
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
