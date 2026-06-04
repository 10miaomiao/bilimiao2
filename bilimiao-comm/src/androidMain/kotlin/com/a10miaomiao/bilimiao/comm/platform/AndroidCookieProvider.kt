package com.a10miaomiao.bilimiao.comm.platform

import android.webkit.CookieManager

class AndroidCookieProvider : CookieProvider {
    private val cookieManager by lazy {
        try { CookieManager.getInstance() } catch (e: Exception) { null }
    }

    override fun getCookie(url: String?): String {
        return cookieManager?.getCookie(url) ?: ""
    }

    override fun setCookie(domain: String, cookie: String) {
        cookieManager?.setCookie(domain, cookie)
    }

    override fun removeAll() {
        cookieManager?.removeAllCookies(null)
    }

    override fun removeSessionCookies() {
        cookieManager?.removeSessionCookies(null)
    }

    override fun flush() {
        cookieManager?.flush()
    }
}
