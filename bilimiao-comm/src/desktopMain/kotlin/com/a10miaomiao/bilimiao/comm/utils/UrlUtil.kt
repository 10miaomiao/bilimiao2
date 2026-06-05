package com.a10miaomiao.bilimiao.comm.utils

actual object UrlUtil {

    actual fun autoHttps(url: String) = if ("://" in url) {
        url.replace("http://","https://")
    } else {
        "https:$url"
    }

    actual fun replaceHost(url: String, host: String): String {
        return url.replace(":\\\\?\\/\\\\?\\/[^\\/]+\\\\?\\/".toRegex(), "://${host}/")
    }

}
