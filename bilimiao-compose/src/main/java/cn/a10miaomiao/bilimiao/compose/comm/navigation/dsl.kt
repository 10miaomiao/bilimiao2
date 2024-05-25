package cn.a10miaomiao.bilimiao.compose.comm.navigation

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.view.View
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

fun View.openSearch(
    mode: Int,
    keyword: String,
    name: String,
) {
    val activity = context as? Activity ?: return
    val intent = Intent(activity, Class.forName("com.a10miaomiao.bilimiao.activity.SearchActivity"))
    val options = ActivityOptions.makeSceneTransitionAnimation(
        activity,
        android.util.Pair(this, "shareElement"),
    ).toBundle()
    intent.putExtra("keyword", keyword)
    intent.putExtra("mode", mode)
    intent.putExtra("name", name)
    activity.startActivityForResult(intent, 1234, options)
}