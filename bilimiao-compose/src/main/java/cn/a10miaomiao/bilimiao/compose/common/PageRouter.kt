package cn.a10miaomiao.bilimiao.compose.common

import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.navOptions
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
//import cn.a10miaomiao.bilimiao.compose.base.navigate

class PageRouter(
    private val hostController: NavHostController
) {

    fun <T : ComposePage> navigate(
        route: T,
        navOptions: NavOptions? = null,
        navigatorExtras: Navigator.Extras? = null
    ) {
        hostController.navigate(route, navOptions, navigatorExtras)
    }

    fun <T : ComposePage> navigate(
        route: T,
        builder: NavOptionsBuilder.() -> Unit
    ) {
        navigate(route, navOptions(builder))
    }
}