package cn.a10miaomiao.bilimiao.compose.common.navigation

import androidx.navigation3.runtime.NavKey
import cn.a10miaomiao.bilimiao.compose.base.ComposePage

/**
 * Desktop 平台：解析 deep link URI 为 [ComposePage] 并加入 backstack。
 * bilimiao:// 与 bilibili:// scheme 的解析统一走 [BilibiliNavigation]。
 */
internal actual fun navigateDeepLink(backStack: BottomBarBackStack, deepLink: String): Boolean {
    val page = BilibiliNavigation.resolveUri(deepLink) ?: return false
    backStack.navigate(page as NavKey)
    return true
}
