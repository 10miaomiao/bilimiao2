package cn.a10miaomiao.bilimiao.compose.components.player

import android.view.ViewOutlineProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.a10miaomiao.bilimiao.comm.delegate.player.BiliExoPlayerMediampPlayer
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.exoplayer.compose.ExoPlayerMediampPlayerSurface

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
        // Compose 的 AndroidView 不裁剪内容（官方 KDoc 明确说明），media3 的 ZOOM（「全屏裁减」）
        // 会把 AspectRatioFrameLayout 测得比 PlayerView 大；SurfaceView 的画面是独立合成 layer，
        // 只能通过"在窗口上打洞"露出，而这个洞是一次普通绘制、受 canvas clip 约束。
        // 因此把 PlayerView 的绘制裁剪到自身边界，洞就不会画到播放器外面，溢出画面被窗口像素遮住。
        clipToOutline = true
        outlineProvider = ViewOutlineProvider.BOUNDS
    }
}
