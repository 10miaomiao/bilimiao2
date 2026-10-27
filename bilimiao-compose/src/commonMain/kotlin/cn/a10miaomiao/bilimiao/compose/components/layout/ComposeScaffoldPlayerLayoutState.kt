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
 * @param anchorEnabled 播放器落点是否启用（落点所属视频就是当前播放视频）
 * @param anchorBounds 播放器落点矩形，用于判断它是否还在视口内
 * @param viewport 宿主布局在根坐标系中的可视矩形；落点完全移出视口时不再按锚点布局，
 *   播放器回到悬浮窗/嵌入式，避免被带到屏幕外
 * @param orientation 窄窗口（compact）为 [ORIENTATION_PORTRAIT]，宽窗口为 [ORIENTATION_LANDSCAPE]
 */
internal fun calculatePlayerDisplayMode(
    showPlayer: Boolean,
    fullScreenPlayer: Boolean,
    pictureInPicture: Boolean,
    anchorEnabled: Boolean,
    anchorBounds: Rect?,
    viewport: Rect,
    orientation: Int,
): PlayerDisplayMode = when {
    !showPlayer -> PlayerDisplayMode.Hidden
    pictureInPicture -> PlayerDisplayMode.PictureInPicture
    fullScreenPlayer -> PlayerDisplayMode.Fullscreen
    // 落点可用 = 启用 + 已上报 + 还在可视区域内
    anchorEnabled && anchorBounds != null && isAnchorInViewport(anchorBounds, viewport) ->
        PlayerDisplayMode.AnchorOverlay
    orientation == ORIENTATION_PORTRAIT -> PlayerDisplayMode.EmbeddedPortrait
    orientation == ORIENTATION_LANDSCAPE -> PlayerDisplayMode.FloatingLandscape
    else -> PlayerDisplayMode.Hidden
}

/**
 * 落点是否还在可视区域内（有交集即算）。
 *
 * 视口尚未测量到（宽高为 0）时不做限制，避免首帧把可用落点误判成屏幕外。
 */
private fun isAnchorInViewport(anchorBounds: Rect, viewport: Rect): Boolean {
    if (viewport.width <= 0f || viewport.height <= 0f) {
        return true
    }
    return anchorBounds.overlaps(viewport)
}

data class ComposeScaffoldPlayerLayoutState(
    val showPlayer: Boolean,
    val fullScreenPlayer: Boolean,
    val orientation: Int,
    val portraitState: PlayerPortraitLayoutState,
    val floatingState: PlayerFloatingLayoutState,
    val playerVideoRatio: Float,
    /** 播放器落点是否启用 */
    val anchorEnabled: Boolean = false,
    /** 播放器落点矩形（启用时才有意义） */
    val anchorBounds: Rect? = null,
    /** 宿主可视矩形，用于判断落点是否还在屏幕内 */
    val viewport: Rect = Rect.Zero,
    val pictureInPicture: Boolean = false,
) {
    val displayMode: PlayerDisplayMode
        get() = calculatePlayerDisplayMode(
            showPlayer = showPlayer,
            fullScreenPlayer = fullScreenPlayer,
            pictureInPicture = pictureInPicture,
            anchorEnabled = anchorEnabled,
            anchorBounds = anchorBounds,
            viewport = viewport,
            orientation = orientation,
        )
}
