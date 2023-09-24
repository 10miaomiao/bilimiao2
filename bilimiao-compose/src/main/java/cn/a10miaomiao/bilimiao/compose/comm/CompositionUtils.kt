package cn.a10miaomiao.bilimiao.compose.comm

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.a10miaomiao.bilimiao.store.WindowStore

@Composable
fun WindowStore.Insets.toPaddingValues(): PaddingValues {
    return remember(this) {
        PaddingValues.Absolute(
            left = leftDp.dp,
            right = rightDp.dp,
            top = topDp.dp,
            bottom = bottomDp.dp,
        )
    }
}