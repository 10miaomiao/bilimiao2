package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.runtime.Composable

/**
 * Android：直接使用系统返回机制
 *
 * 系统返回键/返回手势、弹窗（Dialog / ModalBottomSheet）窗口内的返回，
 * 都由 `OnBackPressedDispatcher` 分发，这里不需要应用自己的返回栈。
 */
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}
