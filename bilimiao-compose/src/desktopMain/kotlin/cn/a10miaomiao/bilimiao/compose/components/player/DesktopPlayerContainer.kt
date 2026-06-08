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
import androidx.compose.ui.graphics.Color
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.DesktopPlayerDelegate
import org.kodein.di.compose.rememberInstance

@Composable
fun DesktopPlayerContainer(
    modifier: Modifier = Modifier,
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

            // 顶栏控制器
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
            ) {
                // 关闭按钮
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

            // 底部控制栏
            DesktopPlayerControls(
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                playbackSpeed = 1.0f,
                onPlayPause = {
                    if (isPlaying) {
                        playerDelegate.pause()
                    } else {
                        playerDelegate.resume()
                    }
                },
                onSeek = { playerDelegate.seekTo(it) },
                onSpeedChange = { playerDelegate.setPlaybackSpeed(it) },
                onFullscreen = { /* TODO: 全屏逻辑 */ },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
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
