package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.openani.mediamp.MediampPlayer

/**
 * 视频播放器画面 Composable（expect/actual）
 *
 * 仅渲染视频画面本身，不包含控制栏、弹幕等 UI。
 * 安卓端使用 [org.openani.mediamp.exoplayer.compose.ExoPlayerMediampPlayerSurface]，
 * 桌面端使用 [org.openani.mediamp.compose.MediampPlayerSurface] (mpv/vlc)。
 *
 * 参考实现：animeko `me.him188.ani.app.videoplayer.ui.VideoPlayer`
 *
 * @param player mediamp 播放器实例
 * @param modifier 布局修饰符
 */
@Composable
expect fun VideoPlayer(
    player: MediampPlayer,
    modifier: Modifier = Modifier,
)
