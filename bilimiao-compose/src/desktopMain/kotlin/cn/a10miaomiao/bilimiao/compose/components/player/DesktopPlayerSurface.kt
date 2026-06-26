package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.compose.MediampPlayerSurface
import org.openani.mediamp.mpv.MpvMediampPlayer
import org.openani.mediamp.mpv.compose.MpvD3D12GpuSurface

@Composable
fun DesktopPlayerSurface(
    player: MediampPlayer,
    modifier: Modifier = Modifier,
) {
    println("[GPU] DesktopPlayerSurface called, player=${player::class.qualifiedName}")
    MediampPlayerSurface(player, modifier)
}
