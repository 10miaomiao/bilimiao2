package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max

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

private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}

internal val LocalContentInsets = staticCompositionLocalOf<ContentInsets> {
    noLocalProvidedFor("ContentInsets")
}

@Composable
fun localContentInsets() = LocalContentInsets.current

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
