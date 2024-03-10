package com.a10miaomiao.bilimiao.comm.network

import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import io.grpc.MethodDescriptor
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BiliGRPCHttp<ReqT, RespT> internal constructor(
    private val grpcMethod: MethodDescriptor<ReqT, RespT>,
    private val reqMessage: ReqT,
) {

    var baseUrl = ApiHelper.GRPC_BASE

    private val client = OkHttpClient()

    var needToken = true

    private fun Request.Builder.addHeaders(): Request.Builder {
        val token = BilimiaoCommApp.commApp.loginInfo?.token_info?.access_token ?: ""
        if (needToken && token.isNotBlank()) {
            addHeader(BiliHeaders.Authorization, BiliHeaders.Identify + " " + token)
            BilimiaoCommApp.commApp.loginInfo?.token_info?.let{
                addHeader(BiliHeaders.BiliMid, it.mid.toString())
            }
        }
        addHeader(BiliHeaders.UserAgent, ApiHelper.USER_AGENT)
        addHeader(BiliHeaders.AppKey, BiliGRPCConfig.mobileApp)
        addHeader(BiliHeaders.BiliDevice, BiliGRPCConfig.getDeviceBin())
        addHeader(BiliHeaders.BiliFawkes, BiliGRPCConfig.getFawkesreqBin())
        addHeader(BiliHeaders.BiliLocale, BiliGRPCConfig.getLocaleBin())
        addHeader(BiliHeaders.BiliMeta, BiliGRPCConfig.getMetadataBin(token))
        addHeader(BiliHeaders.BiliNetwork, BiliGRPCConfig.getNetworkBin())
        addHeader(BiliHeaders.BiliRestriction, BiliGRPCConfig.getRestrictionBin())
        addHeader(BiliHeaders.GRPCAcceptEncodingKey, BiliHeaders.GRPCAcceptEncodingValue)
        addHeader(BiliHeaders.GRPCTimeOutKey, BiliHeaders.GRPCTimeOutValue)
        addHeader(BiliHeaders.Envoriment, BiliGRPCConfig.envorienment)
        addHeader(BiliHeaders.TransferEncodingKey, BiliHeaders.TransferEncodingValue)
        addHeader(BiliHeaders.TEKey, BiliHeaders.TEValue)
        addHeader(BiliHeaders.Buvid, BilimiaoCommApp.commApp.getBilibiliBuvid())
        return this
    }

    private fun buildRequest(): Request {
        val url = baseUrl + grpcMethod.fullMethodName.replace("interfaces", "interface")
        val messageBytes = grpcMethod.streamRequest(reqMessage).readBytes()
        // 校验用?第五位为数组长度
        val stateBytes = byteArrayOf(0, 0, 0, 0, messageBytes.size.toByte())
        // 合并两个字节数组
        val bodyBytes = ByteArray(stateBytes.size + messageBytes.size)
        System.arraycopy(stateBytes, 0, bodyBytes, 0, stateBytes.size)
        System.arraycopy(messageBytes, 0, bodyBytes, stateBytes.size, messageBytes.size)

        val body = bodyBytes.toRequestBody(
            BiliHeaders.GRPCContentType.toMediaType()
        )
        return Request.Builder()
            .url(url)
            .addHeaders()
            .post(body)
            .build()
    }

    private fun parseResponse(res: Response): RespT {
        val inputStream = res.body!!.byteStream()
        inputStream.skip(5L)
        return grpcMethod.parseResponse(inputStream)
    }

    fun call(): RespT {
        val req = buildRequest()
        val res = client.newCall(req).execute()
        return parseResponse(res)
    }

    suspend fun awaitCall(): RespT{
        return suspendCoroutine { continuation ->
            val req = buildRequest()
            client.newCall(req).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }
                override fun onResponse(call: Call, response: Response) {
                    try {
                        continuation.resume(parseResponse(response))
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }
            })
        }
    }
}

fun <ReqT, RespT> MethodDescriptor<ReqT, RespT>.request(
    reqMessage: ReqT,
    biliGRPCHttpBuilder: (BiliGRPCHttp<ReqT, RespT>.() -> Unit)? = null
): BiliGRPCHttp<ReqT, RespT> {
    return BiliGRPCHttp(
        this,
        reqMessage,
    ).apply {
        biliGRPCHttpBuilder?.invoke(this)
    }
}

