package cn.a10miaomiao.bilimiao.compose.components.layout

import cn.a10miaomiao.bilimiao.compose.ORIENTATION_LANDSCAPE
import cn.a10miaomiao.bilimiao.compose.ORIENTATION_PORTRAIT
import cn.a10miaomiao.bilimiao.compose.PlayerFloatingLayoutState
import cn.a10miaomiao.bilimiao.compose.PlayerPortraitLayoutState

enum class PlayerDisplayMode {
    Hidden,
    EmbeddedPortrait,
    FloatingLandscape,
    Fullscreen,
}

data class ComposeScaffoldPlayerLayoutState(
    val showPlayer: Boolean,
    val fullScreenPlayer: Boolean,
    val orientation: Int,
    val portraitState: PlayerPortraitLayoutState,
    val floatingState: PlayerFloatingLayoutState,
    val playerVideoRatio: Float,
) {
    val displayMode: PlayerDisplayMode
        get() = when {
            !showPlayer -> PlayerDisplayMode.Hidden
            fullScreenPlayer -> PlayerDisplayMode.Fullscreen
            orientation == ORIENTATION_PORTRAIT -> PlayerDisplayMode.EmbeddedPortrait
            orientation == ORIENTATION_LANDSCAPE -> PlayerDisplayMode.FloatingLandscape
            else -> PlayerDisplayMode.Hidden
        }
}
