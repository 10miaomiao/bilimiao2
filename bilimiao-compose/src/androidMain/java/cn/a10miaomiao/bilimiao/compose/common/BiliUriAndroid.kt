package cn.a10miaomiao.bilimiao.compose.common

import android.net.Uri

actual class BiliUri actual constructor(rawUri: String) {
    val androidUri: Uri = Uri.parse(rawUri)
    actual val scheme: String? get() = androidUri.scheme
    actual val host: String? get() = androidUri.host
    actual val path: String? get() = androidUri.path
    actual val queryParameterNames: Set<String> get() = androidUri.queryParameterNames
    actual fun getQueryParameter(key: String): String? = androidUri.getQueryParameter(key)
    actual override fun toString(): String = androidUri.toString()
}

actual fun parseUri(uriString: String): BiliUri = BiliUri(uriString)
