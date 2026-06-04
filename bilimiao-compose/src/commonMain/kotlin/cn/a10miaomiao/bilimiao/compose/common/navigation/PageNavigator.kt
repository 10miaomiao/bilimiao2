package cn.a10miaomiao.bilimiao.compose.common.navigation

import cn.a10miaomiao.bilimiao.compose.base.ComposePage

interface PageNavigator {
    fun <T : ComposePage> navigate(route: T)
    fun canPopBackStack(): Boolean
    fun popBackStack(): Boolean
    fun <T : ComposePage> popBackStack(route: T, inclusive: Boolean, saveState: Boolean = false)
    fun navigateByUri(uriString: String): Boolean
    fun launchWebBrowser(url: String)
}
