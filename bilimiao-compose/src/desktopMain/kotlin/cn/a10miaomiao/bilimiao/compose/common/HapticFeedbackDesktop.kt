package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.runtime.Composable

@Composable
actual fun rememberHapticFeedback(): HapticFeedbackPerformer {
    return object : HapticFeedbackPerformer {
        override fun perform(type: HapticFeedbackType) {
            // No-op on desktop
        }
    }
}
