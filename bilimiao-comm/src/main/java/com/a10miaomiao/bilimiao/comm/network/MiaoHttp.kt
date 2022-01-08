package com.a10miaomiao.bilimiao.comm.network

import android.util.Log
import android.webkit.CookieManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*

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

//    fun rxCall() = Observable.create<Response> {
//        var response = call()
//        if (response.isSuccessful) {
//            it.onNext(response)
//            it.onComplete()
//        } else {
//            it.onError(Exception("MiaoHttp: error"))
//        }
//    }

    companion object {
        fun request(url: String? = null, init: (MiaoHttp.() -> Unit)? = null) = MiaoHttp(url).apply {
            init?.invoke(this)
        }

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
