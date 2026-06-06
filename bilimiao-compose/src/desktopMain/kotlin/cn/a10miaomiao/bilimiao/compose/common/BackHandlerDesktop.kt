package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop has no system back button, no-op
}
