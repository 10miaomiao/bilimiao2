package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import cn.a10miaomiao.bilimiao.compose.common.back.LocalBackDispatcher

/**
 * 桌面端：没有系统返回键，注册到应用自建的返回栈
 *
 * 由 app bar 返回按钮与 Esc 驱动
 * （[cn.a10miaomiao.bilimiao.compose.common.back.AppBackHandler]）。
 */
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    val dispatcher = LocalBackDispatcher.current ?: return
    // 用 rememberUpdatedState 保持注册只发生一次，同时读到最新的 enabled / 回调
    val currentOnBack by rememberUpdatedState(onBack)
    val currentEnabled by rememberUpdatedState(enabled)
    DisposableEffect(dispatcher) {
        val unregister = dispatcher.register(
            isEnabled = { currentEnabled },
            onBack = { currentOnBack() },
        )
        onDispose { unregister() }
    }
}
