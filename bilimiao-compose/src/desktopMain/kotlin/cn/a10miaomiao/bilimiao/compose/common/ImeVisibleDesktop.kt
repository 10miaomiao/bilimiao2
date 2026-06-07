package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.runtime.Composable

@Composable
actual fun isImeVisible(): Boolean {
    // Desktop does not have a system soft keyboard
    return false
}
