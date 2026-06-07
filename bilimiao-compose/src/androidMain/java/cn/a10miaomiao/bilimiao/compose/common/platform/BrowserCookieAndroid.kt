package cn.a10miaomiao.bilimiao.compose.common.platform

import android.webkit.CookieManager

actual fun getBrowserCookie(url: String): String {
    return CookieManager.getInstance().getCookie(url) ?: ""
}
