package com.a10miaomiao.bilimiao.netword

import com.a10miaomiao.bilimiao.entity.RegionTypeDetailsInfo
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import okhttp3.*
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class MiaoHttp(var url: String?) {
    var client = OkHttpClient()
    val requestBuilder = Request.Builder()
    var headers = mapOf<String, String>()
    var body: RequestBody = FormBody.Builder().build()

    fun get(): Response {
        for (key in headers.keys) {
            requestBuilder.addHeader(key, headers[key])
        }
        val request = requestBuilder.get()
                .url(url)
                .build()
        return client.newCall(request).execute()
    }

    fun post(): Response {
        for (key in headers.keys) {
            requestBuilder.addHeader(key, headers[key])
        }
        val request = requestBuilder.post(body)
                .url(url)
                .build()
        return client.newCall(request).execute()
    }

    companion object {
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

        inline fun <reified T> getJson(url: String? = null, noinline init: (MiaoHttp.() -> Unit)? = null) = get(url, gsonConverterFactory<T>(object : TypeToken<T>() {}.type), init)
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

        inline fun <reified T> postJson(url: String? = null, noinline init: (MiaoHttp.() -> Unit)? = null) = post(url, gsonConverterFactory<T>(object : TypeToken<T>() {}.type), init)
        inline fun postString(url: String? = null, noinline init: (MiaoHttp.() -> Unit)? = null) = post(url, { it.body()!!.string() }, init)


        fun <T> gsonConverterFactory(type: Type): (response: Response) -> T = { response ->
            val json_str = response.body()!!.string()
            Gson().fromJson(json_str, type)
        }
    }
}