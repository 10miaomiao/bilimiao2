package com.a10miaomiao.bilimiao.comm.utils

expect object UrlUtil {
    fun autoHttps(url: String): String
    fun replaceHost(url: String, host: String): String
}
