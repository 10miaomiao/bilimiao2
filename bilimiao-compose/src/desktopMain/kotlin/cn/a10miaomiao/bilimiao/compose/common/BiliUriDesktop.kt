package cn.a10miaomiao.bilimiao.compose.common

import java.net.URI

actual class BiliUri actual constructor(rawUri: String) {
    private val uri = URI(rawUri)
    actual val scheme: String? get() = uri.scheme
    actual val host: String? get() = uri.host
    actual val path: String? get() = uri.path
    actual val queryParameterNames: Set<String>
        get() {
            val query = uri.query ?: return emptySet()
            return query.split("&").map { it.substringBefore("=") }.toSet()
        }

    actual fun getQueryParameter(key: String): String? {
        val query = uri.query ?: return null
        return query.split("&")
            .firstOrNull { it.substringBefore("=") == key }
            ?.substringAfter("=", "")
    }

    actual override fun toString(): String = uri.toString()
}

actual fun parseUri(uriString: String): BiliUri = BiliUri(uriString)
