package com.a10miaomiao.bilimiao.netword

import android.util.Log
import android.webkit.CookieManager
import com.a10miaomiao.bilimiao.entity.LoginInfo
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import okhttp3.*
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class MiaoHttp(var url: String? = null) {
    private val TAG = "MiaoHttp"
    private val cookieManager = CookieManager.getInstance()

    var client = OkHttpClient()
    val requestBuilder = Request.Builder()
    var headers = mapOf<String, String>()
    var method = "GET"

    var body: RequestBody? = null
    var formBody: Map<String, String>? = null

    fun call(): Response {
        for (key in headers.keys) {
            requestBuilder.addHeader(key, headers[key])
        }
        requestBuilder.addHeader("referer","https://www.bilibili.com/")
        requestBuilder.addHeader("cookie", (cookieManager.getCookie(url) ?: ""))
        if (body == null && formBody != null) {
            val bodyStr = ApiHelper.urlencode(formBody!!)
            body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded")
                , bodyStr)
        }
        Log.d(TAG, "-----START-$method-----")
        Log.d(TAG, "URL = $url")
        formBody?.let {
            Log.d(TAG, "BODY = $it")
        }
        Log.d(TAG, "------END-$method------")
        val request = requestBuilder.method(method, body)
            .url(url)
            .build()
        return client.newCall(request).execute()
    }

    fun get(): Response {
        method = "GET"
        return call()
    }

    fun post(): Response {
        method = "POST"
        return call()
    }

    fun rxCall() = Observable.create<Response> {
        var response = call()
        if (response.isSuccessful) {
            it.onNext(response)
            it.onComplete()
        } else {
            it.onError(Exception("MiaoHttp: error"))
        }
    }

    companion object {
        fun request(url: String? = null, init: (MiaoHttp.() -> Unit)? = null) = MiaoHttp().apply {
            init?.invoke(this)
        }

        fun <T> get(url: String? = null
                    , converterFactory: (response: Response) -> T
                    , init: (MiaoHttp.() -> Unit)? = null) = Observable.timer(200, TimeUnit.MILLISECONDS) // 延迟200毫秒加载，避免页面切换时动画卡顿
                .map {
                    val http = MiaoHttp(url)
                    if (init != null)
                        http.init()
                    var response = http.get()
                    if (response.isSuccessful) {
                        return@map converterFactory(response)
                    } else {
                        throw Exception("MiaoHttp: error")
                    }
                }

        inline fun <reified T> getJson(url: String? = null, noinline init: (MiaoHttp.() -> Unit)? = null) = get(url, gsonConverterFactory<T>(), init)
        inline fun getString(url: String? = null, noinline init: (MiaoHttp.() -> Unit)? = null) = get(url, { it.body()!!.string() }, init)

        fun <T> post(url: String? = null
                     , converterFactory: (response: Response) -> T
                     , init: (MiaoHttp.() -> Unit)? = null) = Observable.create<T> {
            val http = MiaoHttp(url)
            if (init != null)
                http.init()
            var response = http.post()
            if (response.isSuccessful) {
                it.onNext(converterFactory(response))
                it.onComplete()
            } else {
                it.onError(Exception("MiaoHttp: error"))
            }
        }

        inline fun <reified T> postJson(url: String? = null, noinline init: (MiaoHttp.() -> Unit)? = null) = post(url, gsonConverterFactory<T>(), init)
        inline fun postString(url: String? = null, noinline init: (MiaoHttp.() -> Unit)? = null) = post(url, { it.body()!!.string() }, init)

        inline fun <reified T> gsonConverterFactory(): (response: Response) -> T = { response ->
            val jsonStr = response.body()!!.string()
            Gson().fromJson(jsonStr, object : TypeToken<T>() {}.type)
        }

        inline fun <reified T> Response.gson(): T {
            val jsonStr = this.body()!!.string()
            return Gson().fromJson(jsonStr, object : TypeToken<T>() {}.type)
        }

    }
}
