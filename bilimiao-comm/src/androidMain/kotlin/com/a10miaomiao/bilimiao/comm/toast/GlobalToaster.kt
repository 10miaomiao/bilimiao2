package com.a10miaomiao.bilimiao.comm.toast

import com.dokar.sonner.TextToastAction
import com.dokar.sonner.ToasterDefaults
import com.dokar.sonner.ToasterState
import kotlin.time.Duration

object GlobalToaster {
    private var _state: ToasterState? = null

    fun init(state: ToasterState) {
        _state = state
    }

    fun show(
        message: String,
        duration: Duration = ToasterDefaults.DurationShort,
    ) {
        _state?.show(message = message, duration = duration)
    }

    fun showLong(
        message: String,
    ) {
        _state?.show(message = message, duration = ToasterDefaults.DurationLong)
    }

    fun showWithAction(
        message: String,
        actionLabel: String,
        duration: Duration = ToasterDefaults.DurationLong,
        onAction: () -> Unit,
    ) {
        _state?.show(
            message = message,
            duration = duration,
            action = TextToastAction(text = actionLabel) { onAction() },
        )
    }
}
