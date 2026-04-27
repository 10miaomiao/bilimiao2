package cn.a10miaomiao.bilimiao.compose.components.layout

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.common.ContentInsets
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarConfig
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarOrientation
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarState
import kotlin.math.min

internal data class ComposeScaffoldPlayerLayoutState(
    val showPlayer: Boolean,
    val fullScreenPlayer: Boolean,
    val orientation: Int,
    val smallModePlayerCurrentHeight: Int,
    val smallModePlayerMinHeight: Int,
    val playerSmallShowAreaWidth: Int,
    val playerSmallShowAreaHeight: Int,
    val playerVideoRatio: Float,
)

internal data class ComposeScaffoldLayoutResult(
    val contentInsets: ContentInsets,
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

internal fun Density.calculateComposeScaffoldLayout(
    viewportWidth: Dp,
    viewportHeight: Dp,
    rawWindowInsets: ContentInsets,
    appBarState: AppBarState?,
    playerState: ComposeScaffoldPlayerLayoutState,
): ComposeScaffoldLayoutResult {
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
    val verticalAppBarHeight = if (hasVerticalAppBar) {
        AppBarConfig.Height
    } else {
        0.dp
    }

    val playerBounds = calculatePlayerBounds(
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
        playerState = playerState,
    )

    val contentInsets = ContentInsets(
        left = 0.dp,
        top = if (
            playerState.showPlayer &&
            !playerState.fullScreenPlayer &&
            playerState.orientation == 1 &&
            playerBounds != null
        ) {
            playerBounds.bottom.toDp()
        } else {
            rawWindowInsets.top
        },
        right = rawWindowInsets.right,
        bottom = rawWindowInsets.bottom + verticalAppBarHeight,
    )

    val contentBounds = Rect(
        left = (rawWindowInsets.left + horizontalAppBarWidth).toPx(),
        top = 0f,
        right = (viewportWidthPx).toFloat(),
        bottom = (viewportHeightPx).toFloat(),
    )

    val appBarVerticalBounds = if (hasVerticalAppBar) {
        val appBarHeightPx = AppBarConfig.Height.roundToPx()
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
        val appBarWidthPx = horizontalAppBarWidth.roundToPx()
        Rect(
            left = rawWindowInsets.left.toPx(),
            top = rawWindowInsets.top.toPx(),
            right = rawWindowInsets.left.toPx() + appBarWidthPx,
            bottom = viewportHeightPx.toFloat() - rawWindowInsets.bottom.toPx(),
        )
    } else {
        null
    }

    return ComposeScaffoldLayoutResult(
        contentInsets = contentInsets,
        contentBounds = contentBounds,
        appBarVerticalBounds = appBarVerticalBounds,
        appBarHorizontalBounds = appBarHorizontalBounds,
        playerBounds = playerBounds,
    )
}

private fun Density.calculatePlayerBounds(
    viewportWidth: Dp,
    viewportHeight: Dp,
    playerState: ComposeScaffoldPlayerLayoutState,
): Rect? {
    if (!playerState.showPlayer) {
        return null
    }
    if (playerState.fullScreenPlayer) {
        return Rect(
            left = 0f,
            top = 0f,
            right = viewportWidth.roundToPx().toFloat(),
            bottom = viewportHeight.roundToPx().toFloat(),
        )
    }
    return when (playerState.orientation) {
        1 -> {
            val maxHeightByRatio = viewportWidth.value / playerState.playerVideoRatio
            val maxHeight = min(maxHeightByRatio, viewportHeight.value / 2f).dp
            val minHeight = playerState.smallModePlayerMinHeight.toDp()
                .coerceAtLeast(200.dp)
            val targetHeight = if (playerState.smallModePlayerCurrentHeight > 0) {
                playerState.smallModePlayerCurrentHeight.toDp().coerceAtMost(maxHeight)
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
        2 -> {
            val width = if (playerState.playerSmallShowAreaWidth > 0) {
                (playerState.playerSmallShowAreaWidth / density).dp
            } else {
                480.dp
            }
            val height = if (playerState.playerSmallShowAreaHeight > 0) {
                (playerState.playerSmallShowAreaHeight / density).dp
            } else {
                270.dp
            }
            Rect(
                left = 0f,
                top = 0f,
                right = width.roundToPx().toFloat(),
                bottom = height.roundToPx().toFloat(),
            )
        }
        else -> null
    }
}
