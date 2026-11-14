package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.runtime.Composable

/**
 * 返回事件处理（跨平台）
 *
 * 同一个 API，各平台落到各自的原生机制：
 * - Android：`androidx.activity.compose.BackHandler`，交给系统的 `OnBackPressedDispatcher`
 *   （系统返回键/返回手势、弹窗窗口内的返回都由它分发）
 * - 桌面端等没有系统返回的端：注册到应用自建的返回栈
 *   （[cn.a10miaomiao.bilimiao.compose.common.back.LocalBackDispatcher]），
 *   由 app bar 返回按钮与 Esc 驱动
 *
 * @param enabled 是否启用；禁用的处理器不参与分发
 * @param onBack 返回回调
 */
@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
