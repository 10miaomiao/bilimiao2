package com.a10miaomiao.bilimiao.comm.utils

import android.net.Uri

object UrlUtil {

    fun autoHttps(url: String) =if ("://" in url) {
        url.replace("http://","https://")
    } else {
        "https:$url"
    }

    fun getQueryKeyValueMap(uri: Uri): HashMap<String, String> {
        val keyValueMap = HashMap<String, String>()
        var key: String
        var value: String

        val keyNamesList = uri.queryParameterNames
        val iterator = keyNamesList.iterator()

        while (iterator.hasNext()) {
            key = iterator.next() as String
            value = uri.getQueryParameter(key) as String
            keyValueMap.put(key, value)
        }
        return keyValueMap
    }

    fun replaceHost(url: String, host: String): String {
        return url.replace(":\\\\?\\/\\\\?\\/[^\\/]+\\\\?\\/".toRegex(), "://${host}/")
    }

}