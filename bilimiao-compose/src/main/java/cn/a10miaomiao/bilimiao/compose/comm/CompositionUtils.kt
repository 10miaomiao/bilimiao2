package cn.a10miaomiao.bilimiao.compose.comm

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a10miaomiao.bilimiao.store.WindowStore

@Composable
fun WindowStore.Insets.toPaddingValues(
    addLeftDp: Dp = 0.dp,
    addRightDp: Dp = 0.dp,
    addTopDp: Dp = 0.dp,
    addBottomDp: Dp = 0.dp,
): PaddingValues {
    return remember(this, addLeftDp, addRightDp, addTopDp, addBottomDp) {
        PaddingValues.Absolute(
            left = leftDp.dp + addLeftDp,
            right = rightDp.dp + addRightDp,
            top = topDp.dp + addTopDp,
            bottom = bottomDp.dp + addBottomDp,
        )
    }
}