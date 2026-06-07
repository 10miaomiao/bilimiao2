package cn.a10miaomiao.bilimiao.compose.common

import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

@Composable
actual fun rememberHapticFeedback(): HapticFeedbackPerformer {
    val view = LocalView.current
    return object : HapticFeedbackPerformer {
        override fun perform(type: HapticFeedbackType) {
            val constant = when (type) {
                HapticFeedbackType.SEGMENT_FREQUENT_TICK -> HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
                HapticFeedbackType.LONG_PRESS -> HapticFeedbackConstants.LONG_PRESS
                HapticFeedbackType.GESTURE_END -> HapticFeedbackConstants.GESTURE_END
            }
            view.performHapticFeedback(constant)
        }
    }
}
