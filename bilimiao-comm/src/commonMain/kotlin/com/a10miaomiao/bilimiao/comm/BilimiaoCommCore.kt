package com.a10miaomiao.bilimiao.comm

import com.a10miaomiao.bilimiao.comm.entity.auth.LoginInfo
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders
import com.a10miaomiao.bilimiao.comm.utils.AESUtil
import com.a10miaomiao.bilimiao.comm.utils.MiaoEncryptDecrypt
import java.io.File

class BilimiaoCommCore {
    var loginInfo: LoginInfo? = null
        private set

    private val authFilePath get() = PlatformProviders.context.filesDir.path + "/auth_hd"
    private val key = "Message Word"
    private var _bilibiliBuvid = ""

    fun onCreate() {
        instance = this
        readAuthInfo()
    }

    fun setCookie(cookieInfo: LoginInfo.CookieInfo) {
        val cookieProvider = PlatformProviders.cookieProvider
        cookieInfo.domains.forEach { domain ->
            cookieInfo.cookies.forEach { cookie ->
                cookieProvider.setCookie(domain, cookie.getValue(domain))
            }
        }
        cookieProvider.flush()
    }

    private fun getMiaoEncryptDecrypt(): MiaoEncryptDecrypt {
        val key = getBilibiliBuvid().toByteArray()
        return MiaoEncryptDecrypt(key)
    }

    fun saveAuthInfo(loginInfo: LoginInfo) {
        this.loginInfo = loginInfo
        val miaoED = getMiaoEncryptDecrypt()
        val jsonStr = MiaoJson.toJson(loginInfo)
        val jsonByteArray = jsonStr.toByteArray()
        val secretKey = AESUtil.getKey(key)
        val cipher = AESUtil.encrypt(miaoED.encrypt(jsonByteArray), secretKey)
        val file = File(authFilePath)
        file.writeBytes(cipher)
        loginInfo.cookie_info?.let { setCookie(it) }
    }

    private fun readAuthInfo(): LoginInfo? {
        try {
            val miaoED = getMiaoEncryptDecrypt()
            val secretKey = AESUtil.getKey(key)
            val file = File(authFilePath)
            if (!file.exists()) return null
            val cipher = file.readBytes()
            val jsonByteArray = miaoED.decrypt(AESUtil.decrypt(cipher, secretKey))
            val jsonStr = String(jsonByteArray)
            val loginInfo = MiaoJson.fromJson<LoginInfo>(jsonStr)
            this.loginInfo = loginInfo
            return loginInfo
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun deleteAuth() {
        val file = File(authFilePath)
        file.delete()
        val cookieProvider = PlatformProviders.cookieProvider
        cookieProvider.removeSessionCookies()
        cookieProvider.removeAll()
        cookieProvider.flush()
        this.loginInfo = null
    }

    fun getBilibiliBuvid(): String {
        if (_bilibiliBuvid.isNotBlank()) {
            return _bilibiliBuvid
        }
        val buvidFile = File(PlatformProviders.context.filesDir, "buvid")
        _bilibiliBuvid = if (buvidFile.exists()) {
            buvidFile.readText()
        } else {
            ApiHelper.generateBuvid().also { buvidFile.writeText(it) }
        }
        return _bilibiliBuvid
    }

    companion object {
        lateinit var instance: BilimiaoCommCore
        const val APP_NAME = "bilimiao"
    }
}
