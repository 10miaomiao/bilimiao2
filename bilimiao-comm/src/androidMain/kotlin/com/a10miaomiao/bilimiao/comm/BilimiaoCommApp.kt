package com.a10miaomiao.bilimiao.comm

import android.app.Application
import com.a10miaomiao.bilimiao.comm.entity.auth.LoginInfo
import com.a10miaomiao.bilimiao.comm.platform.AndroidBase64Provider
import com.a10miaomiao.bilimiao.comm.platform.AndroidCookieProvider
import com.a10miaomiao.bilimiao.comm.platform.AndroidDeviceInfoProvider
import com.a10miaomiao.bilimiao.comm.platform.AndroidPlatformContext
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders

class BilimiaoCommApp(
    val app: Application
) {
    val loginInfo: LoginInfo? get() = BilimiaoCommCore.instance.loginInfo

    fun saveAuthInfo(loginInfo: LoginInfo) = BilimiaoCommCore.instance.saveAuthInfo(loginInfo)

    fun deleteAuth() = BilimiaoCommCore.instance.deleteAuth()

    fun getBilibiliBuvid(): String = BilimiaoCommCore.instance.getBilibiliBuvid()

    companion object {
        lateinit var commApp: BilimiaoCommApp
        const val APP_NAME = BilimiaoCommCore.APP_NAME
    }

    fun onCreate() {
        commApp = this
        PlatformProviders.context = AndroidPlatformContext(app)
        PlatformProviders.cookieProvider = AndroidCookieProvider()
        PlatformProviders.deviceInfo = AndroidDeviceInfoProvider(app)
        PlatformProviders.base64 = AndroidBase64Provider()
        BilimiaoCommCore.instance = BilimiaoCommCore()
        BilimiaoCommCore.instance.onCreate()
    }
}
