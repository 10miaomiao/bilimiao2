package com.a10miaomiao.bilimiao.comm

import android.app.Application
import android.content.Context
import android.webkit.CookieManager
import com.a10miaomiao.bilimiao.comm.entity.auth.LoginInfo
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.utils.AESUtil
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.google.gson.Gson
import java.io.File

class BilimiaoCommApp(
    val app: Application
) {
    var loginInfo: LoginInfo? = null
        private set

    private val key = "Message Word"
    private var _bilibiliBuvid = ""

    companion object {
        lateinit var commApp: BilimiaoCommApp

        const val APP_NAME = "bilimiao"
    }

    fun onCreate() {
        commApp = this
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
        val secretKey = AESUtil.getKey(key, app)
        val cipher = AESUtil.encrypt(jsonStr, secretKey)
        val file = File(app.filesDir.path + "/auth")
        file.writeBytes(cipher)
        loginInfo.cookie_info?.let { setCookie(it) }
    }

    fun readAuthInfo(): LoginInfo? {
        try {
            val secretKey = AESUtil.getKey(key, app)
            val file = File(app.filesDir.path + "/auth")
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
        val file = File(app.filesDir.path + "/auth")
        file.delete()
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeSessionCookies(null)//移除
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
        this.loginInfo = null
    }


    fun getBilibiliBuvid(): String {
        if (_bilibiliBuvid.isNotBlank()) {
            return _bilibiliBuvid
        }
        val sp = app.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE)
        var buvid = sp.getString("buvid", "")!!
        if (buvid.isBlank()) {
            buvid = ApiHelper.generateBuvid()
            sp.edit().putString("buvid", buvid).apply()
        }
        _bilibiliBuvid = buvid
        return buvid
    }


}