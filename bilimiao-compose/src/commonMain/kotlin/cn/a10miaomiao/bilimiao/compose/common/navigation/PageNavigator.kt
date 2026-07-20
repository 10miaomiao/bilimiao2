package cn.a10miaomiao.bilimiao.compose.common.navigation

import cn.a10miaomiao.bilimiao.compose.base.ComposePage

interface PageNavigator {
    fun <T : ComposePage> navigate(route: T)
    fun canPopBackStack(): Boolean
    fun popBackStack(): Boolean
    fun navigateByUri(uriString: String): Boolean
    fun navigateToVideoInfo(id: String)
    fun launchWebBrowser(url: String)
    fun openScanner(callback: (result: String) -> Unit): Boolean
}
