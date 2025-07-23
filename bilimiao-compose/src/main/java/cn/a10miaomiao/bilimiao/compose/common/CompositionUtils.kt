package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.a10miaomiao.bilimiao.store.WindowStore

@Composable
fun WindowStore.Insets.addPaddingValues(
    addLeft: Dp = 0.dp,
    addRight: Dp = 0.dp,
    addTop: Dp = 0.dp,
    addBottom: Dp = 0.dp,
): PaddingValues {
    return remember(this, addLeft, addRight, addTop, addBottom) {
        PaddingValues.Absolute(
            left = max(leftDp.dp + addLeft, 0.dp),
            right = max(rightDp.dp + addRight, 0.dp),
            top = max(topDp.dp + addTop, 0.dp),
            bottom = max(bottomDp.dp + addBottom, 0.dp),
        )
    }
}

@Composable
fun WindowStore.Insets.toPaddingValues(
    left: Dp? = null,
    right: Dp? = null,
    top: Dp? = null,
    bottom: Dp? = null,
): PaddingValues {
    return remember(this, left, right, top, bottom) {
        PaddingValues.Absolute(
            left = left ?: leftDp.dp,
            right = right ?: rightDp.dp,
            top = top ?: topDp.dp,
            bottom = bottom ?: bottomDp.dp,
        )
    }
}

fun WindowStore.Insets.toWindowInsets(
    addLeft: Dp = 0.dp,
    addRight: Dp = 0.dp,
    addTop: Dp = 0.dp,
    addBottom: Dp = 0.dp,
): WindowInsets {
    return object : WindowInsets {
        override fun getBottom(density: Density): Int {
            return bottom + density.run { addBottom.roundToPx() }
        }

        override fun getLeft(density: Density, layoutDirection: LayoutDirection): Int {
            return left + density.run { addLeft.roundToPx() }
        }

        override fun getRight(density: Density, layoutDirection: LayoutDirection): Int {
            return right + density.run { addRight.roundToPx() }
        }

        override fun getTop(density: Density): Int {
            return top + density.run { addTop.roundToPx() }
        }
    }
}