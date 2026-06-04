package com.a10miaomiao.bilimiao.comm.network

import com.a10miaomiao.bilimiao.comm.BilimiaoCommCore
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MiaoHttp(var url: String? = null) {
    private var client = OkHttpClient()
    private val requestBuilder = Request.Builder()
    val headers = mutableMapOf<String, String>()
    var method = GET

    var body: RequestBody? = null
    var formBody: Map<String, String?>? = null

    private fun buildRequest(): Request {
        requestBuilder.addHeader("User-Agent", ApiHelper.USER_AGENT)
        requestBuilder.addHeader("Referer", ApiHelper.REFERER)
        requestBuilder.addHeader("buvid", BilimiaoCommCore.instance.getBilibiliBuvid())
        if (url?.let { "bilibili.com" in it } == true) {
            requestBuilder.addHeader("env", "prod")
            requestBuilder.addHeader("app-key", "android_hd")
            BilimiaoCommCore.instance.loginInfo?.token_info?.let{
                requestBuilder.addHeader("x-bili-mid", it.mid.toString())
            }
        }
        val cookie = getCookie(url)
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

    private fun getCookie(url: String?): String {
        return url?.let { PlatformProviders.cookieProvider.getCookie(it) } ?: ""
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

        fun request(url: String? = null, init: (MiaoHttp.() -> Unit)? = null) = MiaoHttp(url).apply {
            init?.invoke(this)
        }

        fun Response.string(): String {
            return this.body!!.string()
        }

        inline fun <reified T> Response.json(isLog: Boolean = false): T {
            val jsonStr = this.string()
            if (isLog) {
                miaoLogger() debug jsonStr
            }
            return MiaoJson.fromJson(jsonStr)
        }

        const val GET = "GET"
        const val POST = "POST"

    }
}
