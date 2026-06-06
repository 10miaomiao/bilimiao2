package com.a10miaomiao.bilimiao.comm.utils

expect object UrlUtil {
    fun autoHttps(url: String): String
    fun replaceHost(url: String, host: String): String
}

fun UrlUtil.getQueryKeyValueMap(url: String): HashMap<String, String> {
    val keyValueMap = HashMap<String, String>()
    val queryStart = url.indexOf('?')
    if (queryStart < 0) return keyValueMap
    val query = url.substring(queryStart + 1)
    val fragmentStart = query.indexOf('#')
    val queryWithoutFragment = if (fragmentStart >= 0) query.substring(0, fragmentStart) else query
    for (pair in queryWithoutFragment.split('&')) {
        if (pair.isEmpty()) continue
        val eqIndex = pair.indexOf('=')
        if (eqIndex < 0) {
            keyValueMap[pair] = ""
        } else {
            keyValueMap[pair.substring(0, eqIndex)] = pair.substring(eqIndex + 1)
        }
    }
    return keyValueMap
}
