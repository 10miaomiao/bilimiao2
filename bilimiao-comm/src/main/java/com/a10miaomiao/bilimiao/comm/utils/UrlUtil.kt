package com.a10miaomiao.bilimiao.comm.utils

object UrlUtil {

    fun autoHttps(url: String) =if ("://" in url) {
        url.replace("http://","https://")
    } else {
        "https:$url"
    }

}