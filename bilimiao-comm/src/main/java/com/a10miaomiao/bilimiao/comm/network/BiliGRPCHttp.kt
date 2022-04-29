package com.a10miaomiao.bilimiao.comm.network

import io.grpc.MethodDescriptor
import okhttp3.*
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BiliGRPCHttp<ReqT, RespT> internal constructor(
    private val grpcMethod: MethodDescriptor<ReqT, RespT>,
    private val reqMessage: ReqT,
) {

    companion object {
        const val _grpcBase = "https://grpc.biliapi.net/"

        val userAgent = """
            |bili-universal/62800300
            |os/ios model/${BiliGRPCConfig.model} mobi_app/iphone
            |osVer/${BiliGRPCConfig.osVersion}
            |network/${BiliGRPCConfig.networkType}
            |grpc-objc/1.32.0 grpc-c/12.0.0 (ios; cronet_http)
        """.trimMargin().replace("\n", "")
    }


    private val client = OkHttpClient()

    private fun Request.Builder.addHeaders(): Request.Builder {
        addHeader(BiliHeaders.UserAgent, userAgent)
        addHeader(BiliHeaders.AppKey, BiliGRPCConfig.mobileApp)
        addHeader(BiliHeaders.BiliDevice, BiliGRPCConfig.getDeviceBin())
        addHeader(BiliHeaders.BiliFawkes, BiliGRPCConfig.getFawkesreqBin())
        addHeader(BiliHeaders.BiliLocale, BiliGRPCConfig.getLocaleBin())
        addHeader(BiliHeaders.BiliMeta, BiliGRPCConfig.getMetadataBin(""))
        addHeader(BiliHeaders.BiliNetwork, BiliGRPCConfig.getNetworkBin())
        addHeader(BiliHeaders.BiliRestriction, BiliGRPCConfig.getRestrictionBin())
        addHeader(BiliHeaders.GRPCAcceptEncodingKey, BiliHeaders.GRPCAcceptEncodingValue)
        addHeader(BiliHeaders.GRPCTimeOutKey, BiliHeaders.GRPCTimeOutValue)
        addHeader(BiliHeaders.Envoriment, BiliGRPCConfig.envorienment)
        addHeader(BiliHeaders.TransferEncodingKey, BiliHeaders.TransferEncodingValue)
        addHeader(BiliHeaders.TEKey, BiliHeaders.TEValue)
        return this
    }

    private fun buildRequest(): Request {
        val url = _grpcBase + grpcMethod.fullMethodName
        val messageBytes = grpcMethod.streamRequest(reqMessage).readBytes()
        // 校验用?第五位为数组长度
        val stateBytes = byteArrayOf(0, 0, 0, 0, messageBytes.size.toByte())
        // 合并两个字节数组
        val bodyBytes = ByteArray(stateBytes.size + messageBytes.size)
        System.arraycopy(stateBytes, 0, bodyBytes, 0, stateBytes.size)
        System.arraycopy(messageBytes, 0, bodyBytes, stateBytes.size, messageBytes.size)

        val body = RequestBody.create(
            MediaType.parse(BiliHeaders.GRPCContentType),
            bodyBytes,
        )
        return Request.Builder()
            .url(url)
            .addHeaders()
            .post(body)
            .build()
    }

    private fun parseResponse(res: Response): RespT {
        val inputStream = res.body()!!.byteStream()
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
                    continuation.resume(parseResponse(response))
                }
            })
        }
    }
}

fun <ReqT, RespT> MethodDescriptor<ReqT, RespT>.request(
    reqMessage: ReqT
): BiliGRPCHttp<ReqT, RespT> {
    return BiliGRPCHttp(
        this,
        reqMessage,
    )
}

