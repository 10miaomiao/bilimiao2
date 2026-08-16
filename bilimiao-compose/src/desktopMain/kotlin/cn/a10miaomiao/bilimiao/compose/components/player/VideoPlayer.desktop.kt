package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.compose.MediampPlayerSurface

/**
 * 桌面端 actual：基于 mediamp 的视频画面
 *
 * 通过 [MediampPlayerSurface] 渲染，底层根据 player 实例选择 mpv 或 vlc surface。
 */
@Composable
actual fun VideoPlayer(
    player: MediampPlayer,
    modifier: Modifier,
) {
    MediampPlayerSurface(player, modifier)
}
