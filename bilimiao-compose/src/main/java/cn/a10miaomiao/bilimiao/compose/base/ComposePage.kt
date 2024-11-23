package cn.a10miaomiao.bilimiao.compose.base

import androidx.compose.runtime.Composable


abstract class ComposePage {

    @Composable
    abstract fun Content()

}

//fun NavHostController.navigate(
//    page: ComposePage,
//    navOptions: NavOptions? = null
//) = navigate(page.url(), navOptions)
//
//inline fun <T : ComposePage> NavHostController.navigate(
//    page: T,
//    navOptions: NavOptions? = null,
//    initArgs: T.() -> Unit,
//) = navigate(page.also(initArgs), navOptions)