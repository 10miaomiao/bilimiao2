package cn.a10miaomiao.bilimiao.compose.base

import android.os.Bundle
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import cn.a10miaomiao.bilimiao.compose.comm.navigation.NavDestinationBuilder
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao

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

}

fun NavHostController.navigate(page: ComposePage) = navigate(page.url())

inline fun <T : ComposePage> NavHostController.navigate(
    page: T,
    initArgs: T.() -> Unit,
) = navigate(page.also(initArgs))