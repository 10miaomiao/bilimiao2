package cn.a10miaomiao.bilimiao.compose.common

/**
 * 跨平台 URI 封装，替代 android.net.Uri
 */
expect class BiliUri(rawUri: String) {
    val scheme: String?
    val host: String?
    val path: String?
    val queryParameterNames: Set<String>
    fun getQueryParameter(key: String): String?
    override fun toString(): String
}

expect fun parseUri(uriString: String): BiliUri
