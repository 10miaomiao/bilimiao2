package com.a10miaomiao.bilimiao.comm.network

import android.content.pm.ApplicationInfo
import android.util.Log
import android.webkit.CookieManager
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MiaoHttp(var url: String? = null) {
    private val TAG = "MiaoHttp"
    private val cookieManager = CookieManager.getInstance()

    var client = OkHttpClient()
    val requestBuilder = Request.Builder()
    var headers = mapOf<String, String>()
    var method = GET

    var body: RequestBody? = null
    var formBody: Map<String, String?>? = null

    private fun buildRequest(): Request {
        for (key in headers.keys) {
            requestBuilder.addHeader(key, headers[key]!!)
        }
        requestBuilder.addHeader("user-agent", ApiHelper.USER_AGENT)
        requestBuilder.addHeader("referer",ApiHelper.REFERER)

        if (url?.let { "bilibili.com" in it } == true) {
            requestBuilder.addHeader("env", "prod")
            requestBuilder.addHeader("app-key", "android")
            requestBuilder.addHeader("x-bili-aurora-eid", "UlMFQVcABlAH")
            requestBuilder.addHeader("x-bili-aurora-zone", "sh001")
            BilimiaoCommApp.commApp.loginInfo?.token_info?.let{
                requestBuilder.addHeader("x-bili-mid", it.mid.toString())
            }
        }

        requestBuilder.addHeader("cookie", (cookieManager.getCookie(url) ?: ""))
        if (body == null && formBody != null) {
            val bodyStr = ApiHelper.urlencode(formBody!!)
            body = bodyStr.toRequestBody(
                "application/x-www-form-urlencoded".toMediaType()
            )
        }
        if (DebugMiao.isDebug) {
            Log.d(TAG, "-----START-$method-----")
            Log.d(TAG, "URL = $url")
            formBody?.let {
                Log.d(TAG, "BODY = $it")
            }
            Log.d(TAG, "------END-$method------")
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
            val jsonStr = response.body!!.string()
            Gson().fromJson(jsonStr, object : TypeToken<T>() {}.type)
        }

        inline fun <reified T> Response.gson(isDebug: Boolean = false): T {
            val jsonStr = this.body!!.string()
            if (isDebug) {
                DebugMiao.log(jsonStr)
            }
            return Gson().fromJson(jsonStr, object : TypeToken<T>() {}.type)
        }

        const val GET = "GET"
        const val POST = "POST"

    }
}
