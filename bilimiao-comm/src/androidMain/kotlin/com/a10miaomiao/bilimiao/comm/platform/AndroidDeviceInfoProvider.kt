package com.a10miaomiao.bilimiao.comm.platform

import android.app.Application
import android.os.Build
import android.webkit.WebSettings

class AndroidDeviceInfoProvider(private val app: Application) : DeviceInfoProvider {
    override val brand: String get() = Build.BRAND
    override val model: String get() = Build.MODEL
    override val osVersion: String get() = Build.VERSION.RELEASE
    override val device: String get() = Build.DEVICE

    override val systemUserAgent: String
        get() {
            var userAgent = ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                try {
                    userAgent = WebSettings.getDefaultUserAgent(app)
                } catch (e: Exception) {
                    userAgent = System.getProperty("http.agent") ?: ""
                }
            } else {
                userAgent = System.getProperty("http.agent") ?: ""
            }
            val sb = StringBuffer()
            var i = 0
            val length = userAgent.length
            while (i < length) {
                val c = userAgent[i]
                if (c <= '' || c >= '') {
                    sb.append(String.format("\\u%04x", c.code))
                } else {
                    sb.append(c)
                }
                i++
            }
            return sb.toString()
        }
}
