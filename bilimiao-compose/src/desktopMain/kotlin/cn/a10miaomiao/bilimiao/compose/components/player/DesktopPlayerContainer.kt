package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.DesktopPlayerDelegate
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
    val duration by playerDelegate.duration.collectAsState()
    val isLoading by playerDelegate.isLoading.collectAsState()
    val errorMessage by playerDelegate.errorMessage.collectAsState()

    Box(
        modifier = modifier.background(Color.Black)
    ) {
        player?.let { p ->
            // 视频画面
            DesktopPlayerSurface(
                player = p,
                modifier = Modifier.fillMaxSize(),
            )

            // 顶栏渐变背景 + 关闭按钮
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

            // 加载指示器
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // 底部渐变背景 + 控制栏
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
                    onPlayPause = {
                        if (isPlaying) {
                            playerDelegate.pause()
                        } else {
                            playerDelegate.resume()
                        }
                    },
                    onSeek = { playerDelegate.seekTo(it) },
                    onSpeedChange = { playerDelegate.setPlaybackSpeed(it) },
                    onFullscreen = onFullscreenToggle,
                )
            }
        }

        // 错误信息
        errorMessage?.let { msg ->
            Text(
                text = msg,
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
