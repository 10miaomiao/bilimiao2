package cn.a10miaomiao.bilimiao.compose.comm.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.ComposeFragment
import java.lang.Exception

typealias NavComposableContent = @Composable AnimatedContentScope.(@JvmSuppressWildcards NavBackStackEntry) -> Unit

infix fun String.arguments(arguments: List<NamedNavArgument>): RouteNameAndArguments {
    return RouteNameAndArguments(this, arguments)
}

infix fun String.content(content: NavComposableContent): NavDestinationBuilder {
    return NavDestinationBuilder(
        route = this,
        content = content,
    )
}

infix fun RouteNameAndArguments.content(content: NavComposableContent): NavDestinationBuilder {
    return NavDestinationBuilder(
        route = this.route,
        arguments = this.arguments,
        content = content,
    )
}

fun Fragment.findComposeNavController(): NavHostController {
    if (this is ComposeFragment) {
        return this.composeNav
    }
    throw IllegalStateException("Fragment $this is not ComposeFragment")
}

fun NavController.tryPopBackStack(): Boolean {
    return try {
        popBackStack()
        true
    } catch (e: Exception) {
        false
    }
}