package com.a10miaomiao.bilimiao.comm

import android.app.Application
import android.content.Context
import android.webkit.CookieManager
import com.a10miaomiao.bilimiao.comm.entity.auth.LoginInfo
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.utils.AESUtil
import com.a10miaomiao.bilimiao.comm.utils.MiaoEncryptDecrypt
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogxmaterialyou.style.MaterialYouStyle
import kotlinx.serialization.encodeToString
import java.io.File

class BilimiaoCommApp(
    val app: Application
) {
    var loginInfo: LoginInfo? = null
        private set

    private val authFilePath get() = app.filesDir.path + "/auth_hd"
    private val key = "Message Word"
    private var _bilibiliBuvid = ""

    companion object {
        lateinit var commApp: BilimiaoCommApp

        const val APP_NAME = "bilimiao"
    }

    fun onCreate() {
        commApp = this
        readAuthInfo()

        DialogX.init(app)
        DialogX.globalStyle = MaterialYouStyle.style()
    }

    fun setCookie(cookieInfo: LoginInfo.CookieInfo) {
        val cookieManager = CookieManager.getInstance()
        cookieInfo.domains.forEach { domain ->
            cookieInfo.cookies.forEach { cookie ->
                cookieManager.setCookie(domain, cookie.getValue(domain))
            }
        }
        cookieManager.flush()
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
        val secretKey = AESUtil.getKey(key, app)
        val cipher = AESUtil.encrypt(miaoED.encrypt(jsonByteArray), secretKey)
        val file = File(authFilePath)
        file.writeBytes(cipher)
        loginInfo.cookie_info?.let { setCookie(it) }
    }

    private fun readAuthInfo(): LoginInfo? {
        try {
            val miaoED = getMiaoEncryptDecrypt()
            val secretKey = AESUtil.getKey(key, app)
            val file = File(authFilePath)
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