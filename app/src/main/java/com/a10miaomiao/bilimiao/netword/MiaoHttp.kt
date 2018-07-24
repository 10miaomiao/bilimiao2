package com.a10miaomiao.bilimiao.netword

import com.a10miaomiao.bilimiao.entity.RegionTypeDetailsInfo
import com.google.gson.Gson
import io.reactivex.Observable
import okhttp3.*
import java.io.IOException

class MiaoHttp(var url: String?) {
    val client = OkHttpClient()
    val requestBuilder = Request.Builder()
    var headers = mapOf<String, String>()

    fun get(): Response {
        for (key in headers.keys) {
            requestBuilder.addHeader(key, headers[key])
        }
        val request = requestBuilder.get()
                .url(url)
                .build()
        return client.newCall(request).execute()
    }

    companion object {
        fun <T> get(url: String? = null
                    , converterFactory: (response: Response) -> T
                    , init: (MiaoHttp.() -> Unit)? = null) = Observable.create<T> {
            val http = MiaoHttp(url)
            if (init != null)
                http.init()
            var response = http.get()
            if (response.isSuccessful) {
                it.onNext(converterFactory(response))
            }
            it.onComplete()
        }

        fun <T> getJson(url: String? = null
                        , classOfT: Class<T>
                        , init: (MiaoHttp.() -> Unit)? = null) = get(url, gsonConverterFactory(classOfT), init)

        fun post() {

        }

        fun <T> gsonConverterFactory(classOfT: Class<T>): (response: Response) -> T = { response ->
            val json_str = response.body()!!.string()
            Gson().fromJson(json_str, classOfT)
        }
    }
}