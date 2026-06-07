package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.runtime.Composable

/**
 * Returns whether the software keyboard (IME) is currently visible.
 */
@Composable
expect fun isImeVisible(): Boolean
