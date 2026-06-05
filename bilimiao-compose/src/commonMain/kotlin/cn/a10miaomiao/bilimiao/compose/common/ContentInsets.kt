package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
