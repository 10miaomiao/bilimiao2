package cn.a10miaomiao.bilimiao.compose.comm

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a10miaomiao.bilimiao.store.WindowStore

@Composable
fun WindowStore.Insets.toPaddingValues(
    addLeft: Dp = 0.dp,
    addRight: Dp = 0.dp,
    addTop: Dp = 0.dp,
    addBottom: Dp = 0.dp,
): PaddingValues {
    return remember(this, addLeft, addRight, addTop, addBottom) {
        PaddingValues.Absolute(
            left = leftDp.dp + addLeft,
            right = rightDp.dp + addRight,
            top = topDp.dp + addTop,
            bottom = bottomDp.dp + addBottom,
        )
    }
}