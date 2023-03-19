package cn.a10miaomiao.bilimiao.compose.comm.navigation

import androidx.navigation.*
import androidx.navigation.compose.ComposeNavigator

class NavDestinationBuilder(
    private val route: String,
    private val arguments: List<NamedNavArgument> = emptyList(),
    private val deepLinks: List<NavDeepLink> = emptyList(),
    private val content: NavComposableContent,
) {

    fun url(): String {
        return route
    }

    fun url(
        params: Map<String, String>
    ): String {
        var url = route
        arguments.forEach {
            url.replace("{$it}", params[it.name]!!)
        }
        return url
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