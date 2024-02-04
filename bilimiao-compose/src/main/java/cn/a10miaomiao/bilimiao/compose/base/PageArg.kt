package cn.a10miaomiao.bilimiao.compose.base

import androidx.navigation.NavType
import androidx.navigation.navArgument

class PageArg<T>(
    val name: String,
    private val argType: NavType<T>,
    private val argDefaultValue: T,
) {

    var value: T? = null

    val namedNavArgument get() = navArgument(name) {
        type = argType
        argDefaultValue?.let { defaultValue = it }
    }

    override fun toString(): String {
        return value?.toString() ?: "{$name}"
    }

}

fun intPageArg(
    name: String,
    defaultValue: Int = 0,
) = PageArg(name, NavType.IntType, defaultValue)

fun stringPageArg(
    name: String,
    defaultValue: String? = null,
) = PageArg(name, NavType.StringType, defaultValue)