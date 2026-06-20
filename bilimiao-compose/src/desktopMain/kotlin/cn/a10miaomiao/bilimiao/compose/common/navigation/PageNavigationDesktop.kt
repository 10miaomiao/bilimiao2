package cn.a10miaomiao.bilimiao.compose.common.navigation

import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController

/**
 * 获取 NavDestination 内部注册的 deepLinks 列表
 * 通过反射访问 NavDestination 的 impl 字段中的 deepLinks 属性
 */
private fun NavDestination.getRegisteredDeepLinks(): List<NavDeepLink> {
    return try {
        // NavDestination 有一个 impl 字段，类型是 NavDestinationImpl
        val implField = NavDestination::class.java.getDeclaredField("impl")
        implField.isAccessible = true
        val impl = implField.get(this) ?: return emptyList()

        println("getRegisteredDeepLinks: impl type = ${impl::class.qualifiedName}")

        // 列出 impl 的所有字段
        println("getRegisteredDeepLinks: impl fields:")
        impl::class.java.declaredFields.forEach { field ->
            println("  - ${field.name}: ${field.type.simpleName}")
        }

        // NavDestinationImpl 内部应该有 deepLinks 字段
        val deepLinksField = impl::class.java.getDeclaredField("deepLinks")
        deepLinksField.isAccessible = true
        val deepLinks = deepLinksField.get(impl)

        println("getRegisteredDeepLinks: deepLinks value = $deepLinks")
        println("getRegisteredDeepLinks: deepLinks type = ${deepLinks?.let { it::class.qualifiedName }}")

        @Suppress("UNCHECKED_CAST")
        (deepLinks as? List<NavDeepLink>) ?: emptyList()
    } catch (e: Exception) {
        println("getRegisteredDeepLinks: error = ${e.message}")
        // 如果反射失败，返回空列表
        emptyList()
    }
}

/**
 * 检查 deepLink 是否匹配 NavDeepLink 的 uriPattern
 */
private fun NavDeepLink.matchesDeepLink(deepLink: String): Boolean {
    return try {
        val uriPattern = this.uriPattern ?: return false

        // 简单的模式匹配
        // 将 uriPattern 转换为正则表达式
        val regex = uriPattern
            .replace(".", "\\.")  // 转义点号
            .replace("*", ".*")  // 通配符
            .replace("\\{[^}]+\\}".toRegex(), "([^/?]+)")  // 参数匹配
            .toRegex()

        // 提取 deepLink 的完整路径（包含 scheme://host/path）
        val fullPath = deepLink.substringBefore("?")

        // 检查是否匹配
        regex.matches(fullPath) || regex.containsMatchIn(fullPath)
    } catch (e: Exception) {
        false
    }
}

/**
 * 递归遍历 NavGraph 中的所有目的地，查找匹配的 DeepLink
 */
private fun NavGraph.findDestinationMatchingDeepLink(deepLink: String): NavDestination? {
    for (destination in this) {
        // 获取目的地注册的 deepLinks
        val deepLinks = destination.getRegisteredDeepLinks()

        // 检查是否有匹配的 deepLink
        val matched = deepLinks.any { it.matchesDeepLink(deepLink) }

        if (matched) {
            println("findDestinationMatchingDeepLink: found match at destination: ${destination.route}")
            return destination
        }

        // 如果是嵌套的 NavGraph，递归查找
        if (destination is NavGraph) {
            val found = destination.findDestinationMatchingDeepLink(deepLink)
            if (found != null) {
                return found
            }
        }
    }
    return null
}

internal actual fun navigateDeepLink(navHostController: NavHostController, deepLink: String): Boolean {
    return runCatching {
        println("navigateDeepLink: $deepLink")

        // 遍历所有目的地，查找匹配的 DeepLink
        val graph = navHostController.graph
        val matchedDestination = graph.findDestinationMatchingDeepLink(deepLink)

        if (matchedDestination != null) {
            println("navigateDeepLink: found matching destination: ${matchedDestination.route}")

            // 获取目的地的路由模式，用于构造导航
            val route = matchedDestination.route
            if (route != null) {
                // 从 deepLink 中提取参数
                val routeWithParams = buildRouteWithParams(deepLink, route)
                println("navigateDeepLink: constructed route: $routeWithParams")

                // 尝试使用构造的路由进行导航
                val result = runCatching {
                    navHostController.navigate(routeWithParams)
                }

                if (result.isSuccess) {
                    println("navigateDeepLink: navigation success")
                    return@runCatching true
                } else {
                    println("navigateDeepLink: route navigation failed: ${result.exceptionOrNull()?.message}")
                }
            }
        } else {
            println("navigateDeepLink: no matching destination found")
        }

        false
    }.getOrDefault(false)
}

/**
 * 根据 deepLink 和路由模式构造带参数的路由
 * 例如：
 * deepLink = "bilibili://author/121423"
 * routePattern = "cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage/{id}"
 * 结果 = "cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage/121423"
 */
private fun buildRouteWithParams(deepLink: String, routePattern: String): String {
    // 提取 deepLink 的路径部分（跳过 scheme://host）
    val pathStart = deepLink.indexOf("://")
    if (pathStart == -1) return routePattern

    val rest = deepLink.substring(pathStart + 3)
    val queryStart = rest.indexOf('?')

    // 跳过 host 部分，只取路径
    val firstSlash = rest.indexOf('/')
    val deepLinkPath = if (firstSlash != -1) {
        val pathEnd = if (queryStart != -1) queryStart else rest.length
        rest.substring(firstSlash, pathEnd)
    } else {
        ""
    }

    val deepLinkSegments = deepLinkPath.split("/").filter { it.isNotEmpty() }

    // 提取路由模式的各个部分
    val routeSegments = routePattern.split("/")

    // 构造路由
    val result = StringBuilder()
    var deepLinkIndex = 0

    for (segment in routeSegments) {
        if (result.isNotEmpty()) {
            result.append("/")
        }

        if (segment.startsWith("{") && segment.endsWith("}")) {
            // 这是一个参数占位符，从 deepLink 中取值
            if (deepLinkIndex < deepLinkSegments.size) {
                result.append(deepLinkSegments[deepLinkIndex])
                deepLinkIndex++
            } else {
                result.append(segment) // 保留占位符
            }
        } else {
            // 这是静态部分
            result.append(segment)
        }
    }

    // 处理查询参数
    if (queryStart != -1) {
        val query = rest.substring(queryStart + 1)
        val queryParams = query.split("&").associate {
            val eq = it.indexOf('=')
            if (eq != -1) it.substring(0, eq) to it.substring(eq + 1)
            else it to ""
        }

        // 从路由模式中提取查询参数占位符
        val routeQueryStart = routePattern.indexOf('?')
        if (routeQueryStart != -1) {
            val routeQuery = routePattern.substring(routeQueryStart + 1)
            val routeQueryParams = routeQuery.split("&")

            val queryParamsList = mutableListOf<String>()
            for (routeParam in routeQueryParams) {
                val eq = routeParam.indexOf('=')
                val key = if (eq != -1) routeParam.substring(0, eq) else routeParam
                val value = if (eq != -1) routeParam.substring(eq + 1) else ""

                if (value.startsWith("{") && value.endsWith("}")) {
                    // 这是一个参数占位符
                    queryParamsList.add("$key=${queryParams[key] ?: ""}")
                } else {
                    queryParamsList.add(routeParam)
                }
            }

            result.append("?")
            result.append(queryParamsList.joinToString("&"))
        }
    }

    return result.toString()
}
