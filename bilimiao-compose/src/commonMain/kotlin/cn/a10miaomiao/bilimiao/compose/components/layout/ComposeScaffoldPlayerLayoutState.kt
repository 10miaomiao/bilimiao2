package cn.a10miaomiao.bilimiao.compose.components.layout

import androidx.compose.ui.geometry.Rect
import cn.a10miaomiao.bilimiao.compose.ORIENTATION_LANDSCAPE
import cn.a10miaomiao.bilimiao.compose.ORIENTATION_PORTRAIT
import cn.a10miaomiao.bilimiao.compose.PlayerFloatingLayoutState
import cn.a10miaomiao.bilimiao.compose.PlayerPortraitLayoutState

enum class PlayerDisplayMode {
    Hidden,
    EmbeddedPortrait,
    FloatingLandscape,
    Fullscreen,
    AnchorOverlay,
}

/**
 * 播放器显示模式判定的唯一实现。
 *
 * 供 [ComposeScaffoldPlayerLayoutState]、PlayerLayer 及外部订阅（经由 PlayerState
 * 写回的状态流）共同使用，保证各处推导一致，避免在入口点重复实现造成判定漂移。
 *
 * @param orientation 窄窗口（compact）为 [ORIENTATION_PORTRAIT]，宽窗口为 [ORIENTATION_LANDSCAPE]
 */
internal fun calculatePlayerDisplayMode(
    showPlayer: Boolean,
    fullScreenPlayer: Boolean,
    anchorBounds: Rect?,
    orientation: Int,
): PlayerDisplayMode = when {
    !showPlayer -> PlayerDisplayMode.Hidden
    fullScreenPlayer -> PlayerDisplayMode.Fullscreen
    anchorBounds != null -> PlayerDisplayMode.AnchorOverlay
    orientation == ORIENTATION_PORTRAIT -> PlayerDisplayMode.EmbeddedPortrait
    orientation == ORIENTATION_LANDSCAPE -> PlayerDisplayMode.FloatingLandscape
    else -> PlayerDisplayMode.Hidden
}

data class ComposeScaffoldPlayerLayoutState(
    val showPlayer: Boolean,
    val fullScreenPlayer: Boolean,
    val orientation: Int,
    val portraitState: PlayerPortraitLayoutState,
    val floatingState: PlayerFloatingLayoutState,
    val playerVideoRatio: Float,
    val anchorBounds: Rect? = null,
) {
    val displayMode: PlayerDisplayMode
        get() = calculatePlayerDisplayMode(
            showPlayer = showPlayer,
            fullScreenPlayer = fullScreenPlayer,
            anchorBounds = anchorBounds,
            orientation = orientation,
        )
}
