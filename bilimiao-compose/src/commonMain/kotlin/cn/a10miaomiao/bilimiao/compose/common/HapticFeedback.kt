package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.runtime.Composable

enum class HapticFeedbackType {
    SEGMENT_FREQUENT_TICK,
    LONG_PRESS,
    GESTURE_END,
}

@Composable
expect fun rememberHapticFeedback(): HapticFeedbackPerformer

interface HapticFeedbackPerformer {
    fun perform(type: HapticFeedbackType)
}
