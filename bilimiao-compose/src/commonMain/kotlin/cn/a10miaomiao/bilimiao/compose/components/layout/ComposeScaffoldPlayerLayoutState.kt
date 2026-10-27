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
    AnchorOverlay,

    /**
     * 画中画（应用外小窗）模式：系统把整个应用窗口缩成小窗，窗口内只应显示播放器画面，
     * 因此播放器铺满整个窗口，页面内容与 appbar 均隐藏。
     */
    PictureInPicture,
}

/**
 * 播放器显示模式判定的唯一实现。
 *
 * 供 [ComposeScaffoldPlayerLayoutState]、PlayerLayer 及外部订阅（经由 PlayerState
 * 写回的状态流）共同使用，保证各处推导一致，避免在入口点重复实现造成判定漂移。
 *
 * @param pictureInPicture 画中画（应用外小窗）状态，优先级最高：此时窗口即小窗，
 *   无论进入前处于全屏、锚点还是小屏模式，都应以铺满小窗的方式显示
 * @param anchorEnabled 播放器落点是否启用（落点所属视频就是当前播放视频）。
 *   这里只依赖布尔值而不是落点矩形：矩形随页面滚动持续变化，若参与判定会让
 *   判定方（宿主脚手架）在滚动时反复重组
 * @param orientation 窄窗口（compact）为 [ORIENTATION_PORTRAIT]，宽窗口为 [ORIENTATION_LANDSCAPE]
 */
internal fun calculatePlayerDisplayMode(
    showPlayer: Boolean,
    fullScreenPlayer: Boolean,
    pictureInPicture: Boolean,
    anchorEnabled: Boolean,
    orientation: Int,
): PlayerDisplayMode = when {
    !showPlayer -> PlayerDisplayMode.Hidden
    pictureInPicture -> PlayerDisplayMode.PictureInPicture
    fullScreenPlayer -> PlayerDisplayMode.Fullscreen
    anchorEnabled -> PlayerDisplayMode.AnchorOverlay
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
    /** 播放器落点是否启用，落点矩形在计算几何时单独传入（见 calculatePlayerBounds） */
    val anchorEnabled: Boolean = false,
    val pictureInPicture: Boolean = false,
) {
    val displayMode: PlayerDisplayMode
        get() = calculatePlayerDisplayMode(
            showPlayer = showPlayer,
            fullScreenPlayer = fullScreenPlayer,
            pictureInPicture = pictureInPicture,
            anchorEnabled = anchorEnabled,
            orientation = orientation,
        )
}
