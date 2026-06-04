package com.a10miaomiao.bilimiao.comm.platform

class JvmCookieProvider : CookieProvider {
    private val cookies = mutableMapOf<String, MutableMap<String, String>>()

    override fun getCookie(url: String?): String {
        if (url == null) return ""
        val domain = extractDomain(url)
        return cookies[domain]?.values?.joinToString("; ") ?: ""
    }

    override fun setCookie(domain: String, cookie: String) {
        val domainCookies = cookies.getOrPut(domain) { mutableMapOf() }
        cookie.split(";").forEach { part ->
            val trimmed = part.trim()
            val eqIdx = trimmed.indexOf('=')
            if (eqIdx > 0) {
                domainCookies[trimmed.substring(0, eqIdx)] = trimmed
            }
        }
    }

    override fun removeAll() {
        cookies.clear()
    }

    override fun removeSessionCookies() {
        cookies.clear()
    }

    override fun flush() {
        // No-op for in-memory store
    }

    private fun extractDomain(url: String): String {
        val host = url.removePrefix("https://").removePrefix("http://").substringBefore("/").substringBefore(":")
        val parts = host.split(".")
        return if (parts.size >= 2) parts.takeLast(2).joinToString(".") else host
    }
}
