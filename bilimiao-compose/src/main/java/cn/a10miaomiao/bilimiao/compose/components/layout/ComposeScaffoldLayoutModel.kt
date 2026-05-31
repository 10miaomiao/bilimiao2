package cn.a10miaomiao.bilimiao.compose.components.layout

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.PlayerFloatingLayoutState
import cn.a10miaomiao.bilimiao.compose.PlayerPortraitLayoutState
import cn.a10miaomiao.bilimiao.compose.common.ContentInsets
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarConfig
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarOrientation
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarState
import kotlin.math.min

internal enum class PlayerDisplayMode {
    Hidden,
    EmbeddedPortrait,
    FloatingLandscape,
    Fullscreen,
}

internal data class ComposeScaffoldPlayerLayoutState(
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
            orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT -> PlayerDisplayMode.EmbeddedPortrait
            orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE -> PlayerDisplayMode.FloatingLandscape
            else -> PlayerDisplayMode.Hidden
        }
}

internal data class ComposeScaffoldGeometryResult(
    val contentBounds: Rect,
    val appBarVerticalBounds: Rect?,
    val appBarHorizontalBounds: Rect?,
    val playerBounds: Rect?,
) {
    val hasVerticalAppBar: Boolean
        get() = appBarVerticalBounds != null

    val hasHorizontalAppBar: Boolean
        get() = appBarHorizontalBounds != null

    val hasPlayer: Boolean
        get() = playerBounds != null
}

internal data class ComposeScaffoldLayoutResult(
    val contentInsets: ContentInsets,
    val geometry: ComposeScaffoldGeometryResult,
) {
    val contentBounds: Rect
        get() = geometry.contentBounds

    val appBarVerticalBounds: Rect?
        get() = geometry.appBarVerticalBounds

    val appBarHorizontalBounds: Rect?
        get() = geometry.appBarHorizontalBounds

    val playerBounds: Rect?
        get() = geometry.playerBounds

    val hasVerticalAppBar: Boolean
        get() = geometry.hasVerticalAppBar

    val hasHorizontalAppBar: Boolean
        get() = geometry.hasHorizontalAppBar

    val hasPlayer: Boolean
        get() = geometry.hasPlayer
}

internal fun Density.calculateComposeScaffoldLayout(
    viewportWidth: Dp,
    viewportHeight: Dp,
    rawWindowInsets: ContentInsets,
    appBarState: AppBarState?,
    playerState: ComposeScaffoldPlayerLayoutState,
): ComposeScaffoldLayoutResult {
    val geometry = calculateComposeScaffoldGeometry(
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
        rawWindowInsets = rawWindowInsets,
        appBarState = appBarState,
        playerState = playerState,
    )
    val contentInsets = calculateComposeScaffoldContentInsets(
        rawWindowInsets = rawWindowInsets,
        playerState = playerState,
        geometry = geometry,
    )
    return ComposeScaffoldLayoutResult(
        contentInsets = contentInsets,
        geometry = geometry,
    )
}

private fun Density.calculateComposeScaffoldGeometry(
    viewportWidth: Dp,
    viewportHeight: Dp,
    rawWindowInsets: ContentInsets,
    appBarState: AppBarState?,
    playerState: ComposeScaffoldPlayerLayoutState,
): ComposeScaffoldGeometryResult {
    val viewportWidthPx = viewportWidth.roundToPx()
    val viewportHeightPx = viewportHeight.roundToPx()
    val hasHorizontalAppBar = appBarState?.visible == true &&
        appBarState.orientation == AppBarOrientation.Horizontal
    val hasVerticalAppBar = appBarState?.visible == true &&
        appBarState.orientation == AppBarOrientation.Vertical &&
        appBarState.barVisible

    val horizontalAppBarWidth = if (hasHorizontalAppBar) {
        AppBarConfig.MenuWidth + AppBarConfig.DividerHeight
    } else {
        0.dp
    }

    val playerBounds = calculatePlayerBounds(
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
        playerState = playerState,
    )

    val embeddedPortraitContentTopPx = if (
        playerState.displayMode == PlayerDisplayMode.EmbeddedPortrait &&
        playerBounds != null
    ) {
        rawWindowInsets.top.toPx() + playerBounds.height
    } else {
        0f
    }
    val contentBounds = Rect(
        left = (rawWindowInsets.left + horizontalAppBarWidth).toPx(),
        top = embeddedPortraitContentTopPx,
        right = viewportWidthPx.toFloat(),
        bottom = embeddedPortraitContentTopPx + viewportHeightPx.toFloat(),
    )

    val appBarVerticalBounds = if (hasVerticalAppBar) {
        val appBarHeightPx = (AppBarConfig.Height + rawWindowInsets.bottom).roundToPx()
        Rect(
            left = 0f,
            top = (viewportHeightPx - appBarHeightPx).toFloat(),
            right = viewportWidthPx.toFloat(),
            bottom = viewportHeightPx.toFloat(),
        )
    } else {
        null
    }

    val appBarHorizontalBounds = if (hasHorizontalAppBar) {
        Rect(
            left = 0f,
            top = 0f,
            right = (rawWindowInsets.left + horizontalAppBarWidth).toPx(),
            bottom = viewportHeightPx.toFloat(),
        )
    } else {
        null
    }

    return ComposeScaffoldGeometryResult(
        contentBounds = contentBounds,
        appBarVerticalBounds = appBarVerticalBounds,
        appBarHorizontalBounds = appBarHorizontalBounds,
        playerBounds = playerBounds,
    )
}

private fun Density.calculateComposeScaffoldContentInsets(
    rawWindowInsets: ContentInsets,
    playerState: ComposeScaffoldPlayerLayoutState,
    geometry: ComposeScaffoldGeometryResult,
): ContentInsets {
    val verticalAppBarInset = if (geometry.hasVerticalAppBar) {
        AppBarConfig.Height
    } else {
        0.dp
    }
    val embeddedPortraitPlayerInset = if (
        playerState.displayMode == PlayerDisplayMode.EmbeddedPortrait &&
        geometry.playerBounds != null
    ) {
        geometry.playerBounds.height.toDp() + rawWindowInsets.top
    } else {
        0.dp
    }
    return ContentInsets(
        left = 0.dp,
        top = if (playerState.displayMode == PlayerDisplayMode.EmbeddedPortrait) {
            0.dp
        } else {
            rawWindowInsets.top
        },
        right = rawWindowInsets.right,
        bottom = rawWindowInsets.bottom + verticalAppBarInset + embeddedPortraitPlayerInset,
    )
}

private fun Density.calculatePlayerBounds(
    viewportWidth: Dp,
    viewportHeight: Dp,
    playerState: ComposeScaffoldPlayerLayoutState,
): Rect? {
    return when (playerState.displayMode) {
        PlayerDisplayMode.Hidden -> null
        PlayerDisplayMode.Fullscreen -> Rect(
            left = 0f,
            top = 0f,
            right = viewportWidth.roundToPx().toFloat(),
            bottom = viewportHeight.roundToPx().toFloat(),
        )
        PlayerDisplayMode.EmbeddedPortrait -> {
            val maxHeightByRatio = viewportWidth.value / playerState.playerVideoRatio
            val maxHeight = min(maxHeightByRatio, viewportHeight.value / 2f).dp
            val minHeight = playerState.portraitState.minHeightPx.toDp().coerceAtLeast(200.dp)
            val targetHeight = if (playerState.portraitState.currentHeightPx > 0) {
                playerState.portraitState.currentHeightPx.toDp().coerceAtMost(maxHeight)
            } else {
                minHeight
            }
            Rect(
                left = 0f,
                top = 0f,
                right = viewportWidth.roundToPx().toFloat(),
                bottom = targetHeight.roundToPx().toFloat(),
            )
        }
        PlayerDisplayMode.FloatingLandscape -> {
            val widthPx = when {
                playerState.floatingState.initialized && playerState.floatingState.widthPx > 0f -> playerState.floatingState.widthPx
                playerState.floatingState.defaultWidthPx > 0f -> playerState.floatingState.defaultWidthPx
                else -> 480.dp.toPx()
            }
            val heightPx = when {
                playerState.floatingState.initialized && playerState.floatingState.heightPx > 0f -> playerState.floatingState.heightPx
                playerState.floatingState.defaultHeightPx > 0f -> playerState.floatingState.defaultHeightPx
                else -> 270.dp.toPx()
            }
            Rect(
                left = 0f,
                top = 0f,
                right = widthPx,
                bottom = heightPx,
            )
        }
    }
}
