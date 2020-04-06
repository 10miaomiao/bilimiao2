package com.a10miaomiao.bilimiao.netword

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.entity.LoginInfo
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.entity.UserInfo
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.bilimiao.utils.RSAUtil
import com.a10miaomiao.bilimiao.utils.SettingUtil
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import java.net.URLEncoder
import java.util.*
import kotlin.collections.ArrayList

object LoginHelper {

    private const val BASE_URL = "https://passport.bilibili.com/"
    private val HEADERS = mapOf(
            "user-agent" to "Mozilla/5.0 BiliMiao/2.0 (10miaomiao@outlook.com)"
    )
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build()
    }

    /**
     * 获取RSA公钥，用于加密密码
     */
    private fun getKey(): Observable<KeyInfo> {
        var url = "${BASE_URL}api/oauth2/getKey?appkey=${ApiHelper.APP_KEY_NEW}"
        url += "&sign=" + ApiHelper.getNewSign(url)
        return MiaoHttp.postJson<ResultInfo<KeyInfo>>(url) {
            client = okHttpClient
            headers = HEADERS
        }.map {
            it.data
        }
    }

    /**
     * 登录
     */
    fun login(username: String, password: String, captcha: String): Observable<ResultInfo<LoginInfo>> {
        return getKey().flatMap {
            val key = it.key.replace("-----BEGIN PUBLIC KEY-----\n", "")
                    .replace("-----END PUBLIC KEY-----\n", "")
            val hash = it.hash
            val encrypt = RSAUtil.decryptByPublicKey(hash + password, key)
            val params = mutableMapOf(
                    "appkey" to ApiHelper.APP_KEY_NEW,
                    "build" to "5390000",
                    "mobi_app" to "android",
                    "platform" to "android",
                    "ts" to ApiHelper.getTimeSpen().toString(),
                    "password" to encrypt.replace("\n", "") ,
                    "username" to username
            )
            if (captcha.isNotEmpty()) {
                params["captcha"] = captcha
            }
            val url = BASE_URL + "api/v3/oauth2/login"
            params["sign"] = ApiHelper.getSing(params, ApiHelper.APP_SECRET_NEW)
            val bodyStr = ApiHelper.urlencode(params)
            DebugMiao.log(bodyStr)
            MiaoHttp.postJson<ResultInfo<LoginInfo>>(url) {
                client = okHttpClient
                headers = HEADERS
                body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded")
                    , bodyStr)
            }
        }
    }

    fun authInfo(access_token: String): Observable<ResultInfo<UserInfo>> {
        var params = mapOf(
                "appkey" to ApiHelper.APP_KEY_NEW,
                "access_key" to access_token,
                "build" to "5310300",
                "mobi_app" to "android",
                "platform" to "android",
                "ts" to ApiHelper.getTimeSpen().toString()
        )
        var url = "https://app.bilibili.com/x/v2/account/mine?" + ApiHelper.urlencode(params)
        url += "&sign=" + ApiHelper.getNewSign(url)
        return MiaoHttp.getJson(url)
    }


    fun getCaptchaImage(url: String) = MiaoHttp.get(url, {
        val bytes = it.body()!!.bytes()
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }, {
        client = okHttpClient
    })


    /**
     * cookie临时保存到内存
     */
    private val cookieJar = object : CookieJar {
        private val cookiesMap = HashMap<String, List<Cookie>>()

        override fun saveFromResponse(httpUrl: HttpUrl, cookiesList: List<Cookie>) {
            val host = httpUrl.host()
            cookiesList.forEach {
                DebugMiao.log(it.name() + "=" + it.value())
            }
            if (cookiesMap.containsKey(host)) {
                cookiesMap.remove(host)
            }
            cookiesMap[host] = cookiesList
        }

        override fun loadForRequest(httpUrl: HttpUrl): List<Cookie> {
            val host = httpUrl.host()
            return cookiesMap[host] ?: ArrayList()
        }
    }

    data class KeyInfo(
            var hash: String,
            var key: String
    )

}