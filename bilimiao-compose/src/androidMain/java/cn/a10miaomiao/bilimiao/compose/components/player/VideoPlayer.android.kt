package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.exoplayer.compose.ExoPlayerMediampPlayerSurface
import com.a10miaomiao.bilimiao.comm.delegate.player.BiliExoPlayerMediampPlayer

/**
 * 安卓端 actual：基于 ExoPlayer 的视频画面
 *
 * 使用 mediamp-exoplayer-compose 提供的 [ExoPlayerMediampPlayerSurface]，
 * 底层为 androidx.media3.ui.PlayerView。关闭控制器（由 Compose 层 [VideoScaffold] 统一管理）。
 *
 * player 可能是 [BiliExoPlayerMediampPlayer]（包装器）或直接 [org.openani.mediamp.exoplayer.ExoPlayerMediampPlayer]，
 * 两种情况都通过 [BiliExoPlayerMediampPlayer.mediampDelegate] 或直接 cast 获取底层实例。
 */
@Composable
actual fun VideoPlayer(
    player: MediampPlayer,
    modifier: Modifier,
) {
    val exoPlayer = when (player) {
        is BiliExoPlayerMediampPlayer -> player.mediampDelegate
        else -> player as org.openani.mediamp.exoplayer.ExoPlayerMediampPlayer
    }
    ExoPlayerMediampPlayerSurface(exoPlayer, modifier) {
        controllerAutoShow = false
        useController = false
        controllerHideOnTouch = false
    }
}
