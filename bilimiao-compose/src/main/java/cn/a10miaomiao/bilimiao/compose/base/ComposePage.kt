package cn.a10miaomiao.bilimiao.compose.base

import android.os.Bundle
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavType
import com.a10miaomiao.bilimiao.comm.navigation.NavHosts
import com.a10miaomiao.bilimiao.comm.navigation.inNavHosts
import com.a10miaomiao.bilimiao.comm.navigation.stopSameUrlCompose

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

fun NavController.navigateToCompose(url:String){
    //是compose内部导航则直接跳转
    //否则通过接口跳转
    if(inNavHosts()){
        apply{
            with(context as? NavHosts ?: return){
                navigateCompose(url)
            }
        }
    } else {
        stopSameUrlCompose(url)?.navigate(url)
    }
}
fun NavController.navigate(page: ComposePage) = navigateToCompose(page.url())

inline fun <T : ComposePage> NavController.navigate(
    page: T,
    initArgs: T.() -> Unit,
) = navigate(page.also(initArgs))