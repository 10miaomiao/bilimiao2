package cn.a10miaomiao.bilimiao.compose.common.navigation

import android.net.Uri
import androidx.navigation.NavHostController

internal actual fun navigateDeepLink(navHostController: NavHostController, deepLink: String): Boolean {
    return runCatching {
        navHostController.navigate(Uri.parse(deepLink))
    }.isSuccess
}
