package com.a10miaomiao.bilimiao.comm.toast

import kotlin.time.Duration

/**
 * 全局轻提示入口（薄壳）。
 *
 * 本对象只做转发，不持有任何数据：真正的 toast 列表在 [MiaoToastState] 里，
 * 由宿主的组合在创建时通过 [init] 注入（见 `bilimiao-compose` 的 `MiaoToastHost` 与 `rememberMiaoToastState`）。
 *
 * 这样设计的好处：state 与宿主同生命周期（每个宿主 `remember` 一份），
 * 换宿主自然得到一个全新的空 state，不需要额外做跨宿主的清理。
 *
 * [init] 之前调用的提示会被直接丢弃（此时还没有界面可以显示）。
 */
object GlobalToaster {
    private var _state: MiaoToastState? = null

    /** 由宿主在组合时注入本宿主的 state */
    fun init(state: MiaoToastState) {
        _state = state
    }

    /** 显示一条短提示（默认 [MiaoToastDefaults.DurationShort]） */
    fun show(
        message: String,
        duration: Duration = MiaoToastDefaults.DurationShort,
        position: ToastPosition = ToastPosition.Bottom,
    ) {
        _state?.show(message = message, duration = duration, position = position)
    }

    /** 显示一条长提示（默认 [MiaoToastDefaults.DurationLong]） */
    fun showLong(
        message: String,
        position: ToastPosition = ToastPosition.Bottom,
    ) {
        _state?.show(
            message = message,
            duration = MiaoToastDefaults.DurationLong,
            position = position,
        )
    }

    /**
     * 显示一条带动作按钮的提示（默认 [MiaoToastDefaults.DurationLong]）。
     *
     * 点击动作按钮时先执行 [onAction]，随后该提示立即消失。
     *
     * 注意 [onAction] 必须是最后一个参数：现有调用方普遍用尾随 lambda 传回调。
     */
    fun showWithAction(
        message: String,
        actionLabel: String,
        duration: Duration = MiaoToastDefaults.DurationLong,
        position: ToastPosition = ToastPosition.Bottom,
        onAction: () -> Unit,
    ) {
        _state?.show(
            message = message,
            duration = duration,
            position = position,
            action = MiaoToastAction(text = actionLabel) { onAction() },
        )
    }
}
