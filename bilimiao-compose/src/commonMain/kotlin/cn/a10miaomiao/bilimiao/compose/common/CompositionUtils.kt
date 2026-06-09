package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarConfig
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarOrientation
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarState

const val ORIENTATION_PORTRAIT = 1
const val ORIENTATION_LANDSCAPE = 2

@Composable
fun WindowInsets.toContentInsets(): ContentInsets {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val leftPx = getLeft(density, layoutDirection)
    val topPx = getTop(density)
    val rightPx = getRight(density, layoutDirection)
    val bottomPx = getBottom(density)
    return remember(leftPx, topPx, rightPx, bottomPx) {
        ContentInsets(
            left = with(density) { leftPx.toDp() },
            top = with(density) { topPx.toDp() },
            right = with(density) { rightPx.toDp() },
            bottom = with(density) { bottomPx.toDp() },
        )
    }
}

fun ContentInsets.toWindowInsets(
    addLeft: Dp = 0.dp,
    addRight: Dp = 0.dp,
    addTop: Dp = 0.dp,
    addBottom: Dp = 0.dp,
): WindowInsets {
    return object : WindowInsets {
        override fun getBottom(density: Density): Int {
            return density.run { (bottom + addBottom).roundToPx() }
        }

        override fun getLeft(density: Density, layoutDirection: LayoutDirection): Int {
            return density.run { (left + addLeft).roundToPx() }
        }

        override fun getRight(density: Density, layoutDirection: LayoutDirection): Int {
            return density.run { (right + addRight).roundToPx() }
        }

        override fun getTop(density: Density): Int {
            return density.run { (top + addTop).roundToPx() }
        }
    }
}

fun calculateMainContentInsets(
    rawWindowInsets: ContentInsets,
    appBarState: AppBarState?,
    showPlayer: Boolean,
    orientation: Int,
): ContentInsets {
    val isHorizontal = appBarState?.visible == true &&
        appBarState.orientation == AppBarOrientation.Horizontal
    if (isHorizontal) {
        return rawWindowInsets
    }
    val verticalBottomChrome = if (appBarState?.visible != false && orientation == ORIENTATION_PORTRAIT) {
        AppBarConfig.Height
    } else {
        0.dp
    }
    return if (orientation == ORIENTATION_PORTRAIT && showPlayer) {
        rawWindowInsets.copy(
            top = 0.dp,
            bottom = rawWindowInsets.top + rawWindowInsets.bottom + verticalBottomChrome,
        )
    } else {
        rawWindowInsets.copy(
            bottom = rawWindowInsets.bottom + verticalBottomChrome,
        )
    }
}

fun bottomSheetContentInsets(
    titleBarHeight: Dp = 48.dp,
): ContentInsets {
    return ContentInsets(top = titleBarHeight)
}
