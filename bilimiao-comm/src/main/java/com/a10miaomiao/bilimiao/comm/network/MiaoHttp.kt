package com.a10miaomiao.bilimiao.comm.network

import android.webkit.CookieManager
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MiaoHttp(var url: String? = null) {
    private val cookieManager = CookieManager.getInstance()

    private var client = OkHttpClient()
    private val requestBuilder = Request.Builder()
    val headers = mutableMapOf<String, String>()
    var method = GET

    var body: RequestBody? = null
    var formBody: Map<String, String?>? = null

    private fun buildRequest(): Request {
        requestBuilder.addHeader("User-Agent", ApiHelper.USER_AGENT)
        requestBuilder.addHeader("Referer", ApiHelper.REFERER)

        if (url?.let { "bilibili.com" in it } == true) {
            requestBuilder.addHeader("env", "prod")
            requestBuilder.addHeader("app-key", "android_hd")
            BilimiaoCommApp.commApp.loginInfo?.token_info?.let{
                requestBuilder.addHeader("x-bili-mid", it.mid.toString())
            }
        }
        val cookie = cookieManager.getCookie(url)
        if (!cookie.isNullOrBlank()) {
            requestBuilder.addHeader("Cookie", cookie)
        }
        for (key in headers.keys) {
            requestBuilder.addHeader(key, headers[key]!!)
        }

        if (body == null && formBody != null) {
            val bodyStr = ApiHelper.urlencode(formBody!!)
            body = bodyStr.toRequestBody(
                "application/x-www-form-urlencoded".toMediaType()
            )
        }
        val req = requestBuilder.method(method, body)
            .url(url!!)
            .build()
        return req
    }

    fun call(): Response {
        val req = buildRequest()
        return client.newCall(req).execute()
    }

    suspend fun awaitCall(): Response{
        miaoLogger().d(
            "method" to method,
            "url" to url,
            "formBody" to formBody
        )
        return suspendCancellableCoroutine { continuation ->
            val req = buildRequest()
            val call = client.newCall(req)
            continuation.invokeOnCancellation {
                call.cancel()
            }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }
                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response)
                }
            })
        }
    }

    fun get(): Response {
        method = GET
        return call()
    }

    fun post(): Response {
        method = POST
        return call()
    }

//    fun <T> responseType<>() {
//
//    }

    companion object {

        private val gson = Gson()

        val kotlinJson = Json {
            ignoreUnknownKeys = true
        }

        fun <T> fromJson(json: String, typeOfT: Type): T {
            try {
                return gson.fromJson(json, typeOfT)
            } catch (e: IllegalStateException) {
                miaoLogger().i("GSON解析出错", json)
                throw e
            }
        }

        inline fun <reified T> fromJson(json: String): T {
            try {
                return kotlinJson.decodeFromString<T>(json)
            } catch (e: IllegalStateException) {
                miaoLogger().i("JSON解析出错", json)
                throw e
            }
        }

        fun request(url: String? = null, init: (MiaoHttp.() -> Unit)? = null) = MiaoHttp(url).apply {
            init?.invoke(this)
        }

        inline fun <reified T> gsonConverterFactory(): (response: Response) -> T = { response ->
            val jsonStr = response.string()
            fromJson(jsonStr, object : TypeToken<T>() {}.type)
        }

        fun Response.string(): String {
            return this.body!!.string()
        }

        inline fun <reified T> String.gson(isLog: Boolean = false): T {
            val jsonStr = this
            if (isLog) {
                miaoLogger() debug jsonStr
            }
            val type = object : TypeToken<T>() {}.type
            return fromJson(jsonStr, type)
        }

        inline fun <reified T> Response.gson(isLog: Boolean = false): T {
            return this.string().gson<T>(isLog)
        }

        inline fun <reified T> Response.json(isLog: Boolean = false): T {
            val jsonStr = this.string()
            if (isLog) {
                miaoLogger() debug jsonStr
            }
            return fromJson(jsonStr)
        }

        const val GET = "GET"
        const val POST = "POST"

    }
}
