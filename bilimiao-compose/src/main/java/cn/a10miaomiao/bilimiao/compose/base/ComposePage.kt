package cn.a10miaomiao.bilimiao.compose.base

import android.os.Bundle
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import cn.a10miaomiao.bilimiao.compose.animation.materialFadeThroughIn
import cn.a10miaomiao.bilimiao.compose.animation.materialFadeThroughOut

abstract class ComposePage {

    abstract val route: String

    open val arguments: List<NamedNavArgument> get() = autoGetArguments()

    open val deepLinks: List<NavDeepLink> get() = emptyList()

    open fun url(): String {
        return route
    }

    open fun url(params: Map<String, String>): String {
        var url = route
        arguments.forEach {
            url = url.replace("{${it.name}}", params[it.name]!!)
        }
        return url
    }

    @Composable
    abstract fun AnimatedContentScope.Content(navEntry: NavBackStackEntry)

    fun <T> Bundle.get(arg: PageArg<T>): T {
        return when (arg.argType) {
            NavType.IntType -> this.getInt(arg.name)
            NavType.IntArrayType -> this.getIntArray(arg.name)
            NavType.LongType -> this.getLong(arg.name)
            NavType.LongArrayType -> this.getLongArray(arg.name)
            NavType.FloatType -> this.getFloat(arg.name)
            NavType.FloatArrayType -> this.getFloatArray(arg.name)
            NavType.BoolType -> this.getBoolean(arg.name)
            NavType.BoolArrayType -> this.getBooleanArray(arg.name)
            NavType.StringType -> this.getString(arg.name)
            NavType.StringArrayType -> this.getStringArray(arg.name)
            else -> this.get(arg.name)
        } as T
    }

    fun <T> Bundle.getOrNull(arg: PageArg<T>): T? {
        return this.get(arg.name) as? T
    }

    infix fun <T> PageArg<T>.set(value: T) {
        this.value = value
    }

    private fun autoGetArguments(): List<NamedNavArgument> {
        val typeName = PageArg::class.java.name
        return this::class.java.methods.filter {
            it.returnType.name == typeName
        }.mapNotNull {
            (it.invoke(this) as? PageArg<*>)?.namedNavArgument
        }
    }

    open fun enterTransition(scope: @JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>): EnterTransition? {
        return materialFadeThroughIn(initialScale = 0.85f)
    }

    open fun exitTransition(scope: @JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>): ExitTransition? {
        return materialFadeThroughOut()
    }

    open fun popEnterTransition(scope: @JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>): EnterTransition? {
        return materialFadeThroughIn(initialScale = 1.15f)
    }

    open fun popExitTransition(scope: @JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>): ExitTransition? {
        return materialFadeThroughOut()
    }

}

fun NavHostController.navigate(page: ComposePage) = navigate(page.url())

inline fun <T : ComposePage> NavHostController.navigate(
    page: T,
    initArgs: T.() -> Unit,
) = navigate(page.also(initArgs))