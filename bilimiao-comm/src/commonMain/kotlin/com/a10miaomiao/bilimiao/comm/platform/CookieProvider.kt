package com.a10miaomiao.bilimiao.comm.platform

interface CookieProvider {
    fun getCookie(url: String?): String
    fun setCookie(domain: String, cookie: String)
    fun removeAll()
    fun removeSessionCookies()
    fun flush()
}
