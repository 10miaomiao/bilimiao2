package cn.a10miaomiao.bilimiao.compose.comm.navigation

import androidx.navigation.*
import androidx.navigation.compose.ComposeNavigator

class NavDestinationBuilder(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList(),
    val deepLinks: List<NavDeepLink> = emptyList(),
    val content: NavComposableContent,
) {

    fun createUrl(

    ): String {
        return ""
    }

    fun build(provider: NavigatorProvider): NavDestination {
        return ComposeNavigator.Destination(provider[ComposeNavigator::class], content).also {
            it.route = route
            arguments.forEach { (argumentName, argument) ->
                it.addArgument(argumentName, argument)
            }
            deepLinks.forEach { deepLink ->
                it.addDeepLink(deepLink)
            }
        }
    }

}