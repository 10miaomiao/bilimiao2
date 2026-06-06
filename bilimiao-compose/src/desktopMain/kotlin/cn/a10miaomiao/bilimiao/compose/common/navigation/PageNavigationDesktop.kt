package cn.a10miaomiao.bilimiao.compose.common.navigation

import androidx.navigation.NavHostController

internal actual fun navigateDeepLink(navHostController: NavHostController, deepLink: String): Boolean {
    return runCatching {
        navHostController.navigate(deepLink)
    }.isSuccess
}
