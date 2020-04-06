package com.a10miaomiao.bilimiao

import android.app.Application
import android.content.res.Configuration
import android.net.Uri
import android.support.v7.app.AppCompatDelegate
import android.util.Base64
import android.webkit.CookieManager
import com.a10miaomiao.bilimiao.entity.LoginInfo
import com.a10miaomiao.bilimiao.utils.AESUtil
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.google.gson.Gson
import java.io.File
import java.net.CookieStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class Bilimiao : Application() {

    var loginInfo: LoginInfo? = null
        private set
    private val key = "Message Word"

    companion object {
        lateinit var app: Bilimiao
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        readAuthInfo()
    }

    private fun setCookie(cookieInfo: LoginInfo.CookieInfo) {
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeSessionCookies(null)//移除
        cookieManager.removeAllCookies(null)
        cookieInfo.domains.forEach { domain ->
            cookieInfo.cookies.forEach { cookie ->
                cookieManager.setCookie(domain, cookie.getValue(domain))
            }
        }
        cookieManager.flush()
    }

    fun saveAuthInfo(loginInfo: LoginInfo) {
        this.loginInfo = loginInfo
        val jsonStr = Gson().toJson(loginInfo)
        val secretKey = AESUtil.getKey(key, this)
        val cipher = AESUtil.encrypt(jsonStr, secretKey)
        val file = File(filesDir.path + "/auth")
        file.writeBytes(cipher)
        setCookie(loginInfo.cookie_info)
    }

    fun readAuthInfo(): LoginInfo? {
        try {
            val secretKey = AESUtil.getKey(key, this)
            val file = File(filesDir.path + "/auth")
            val cipher = file.readBytes()
            val jsonStr = AESUtil.decrypt(cipher, secretKey)
            val loginInfo = Gson().fromJson(jsonStr, LoginInfo::class.java)
            this.loginInfo = loginInfo
            return loginInfo
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun deleteAuth() {
        val file = File(filesDir.path + "/auth")
        file.delete()
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeSessionCookies(null)//移除
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
    }


}