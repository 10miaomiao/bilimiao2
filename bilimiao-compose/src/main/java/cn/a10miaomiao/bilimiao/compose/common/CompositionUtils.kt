package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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

@Immutable
data class ContentInsets(
    val left: Dp = 0.dp,
    val top: Dp = 0.dp,
    val right: Dp = 0.dp,
    val bottom: Dp = 0.dp,
) {
    val leftDp get() = left.value
    val topDp get() = top.value
    val rightDp get() = right.value
    val bottomDp get() = bottom.value
}

@Composable
fun WindowInsets.toContentInsets(): ContentInsets {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    return remember(this, density, layoutDirection) {
        ContentInsets(
            left = with(density) { getLeft(this, layoutDirection).toDp() },
            top = with(density) { getTop(this).toDp() },
            right = with(density) { getRight(this, layoutDirection).toDp() },
            bottom = with(density) { getBottom(this).toDp() },
        )
    }
}

@Composable
fun ContentInsets.addPaddingValues(
    addLeft: Dp = 0.dp,
    addRight: Dp = 0.dp,
    addTop: Dp = 0.dp,
    addBottom: Dp = 0.dp,
): PaddingValues {
    return remember(this, addLeft, addRight, addTop, addBottom) {
        PaddingValues.Absolute(
            left = max(left + addLeft, 0.dp),
            right = max(right + addRight, 0.dp),
            top = max(top + addTop, 0.dp),
            bottom = max(bottom + addBottom, 0.dp),
        )
    }
}

@Composable
fun ContentInsets.toPaddingValues(
    left: Dp? = null,
    right: Dp? = null,
    top: Dp? = null,
    bottom: Dp? = null,
): PaddingValues {
    return remember(this, left, right, top, bottom) {
        PaddingValues.Absolute(
            left = left ?: this.left,
            right = right ?: this.right,
            top = top ?: this.top,
            bottom = bottom ?: this.bottom,
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
    val verticalBottomChrome = if (appBarState?.visible != false && orientation == 1) {
        AppBarConfig.Height
    } else {
        0.dp
    }
    return if (orientation == 1 && showPlayer) {
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
