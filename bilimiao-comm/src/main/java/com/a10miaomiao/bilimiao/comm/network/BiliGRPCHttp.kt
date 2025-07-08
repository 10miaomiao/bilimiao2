package com.a10miaomiao.bilimiao.comm.network

import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import pbandk.Message
import pbandk.decodeFromByteArray
import pbandk.decodeFromStream
import pbandk.encodeToByteArray
import java.io.File
import java.io.IOException
import java.util.zip.GZIPInputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BiliGRPCHttp<ReqT : Message, RespT : Message>(
    val method: GRPCMethod<ReqT, RespT>
) {

    companion object {
        private var baseUrl = ApiHelper.GRPC_BASE

        private val client = OkHttpClient()

        inline fun <ReqT : Message, RespT : Message> request(methodGetter: () -> GRPCMethod<ReqT, RespT>)
            = BiliGRPCHttp(methodGetter())
    }

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
        val url = baseUrl + method.name
        val messageBytes = method.reqMessage.encodeToByteArray()
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
        var inputStream = res.body!!.byteStream()
        inputStream.skip(5L)

        // 手动解压gzip
        if (res.header(BiliHeaders.GRPCEncoding) == BiliHeaders.GRPCEncodingGZIP) {
            inputStream = GZIPInputStream(inputStream)
        }

        return method.respMessageCompanion
            .decodeFromStream(inputStream)
    }

    suspend fun awaitCall(): RespT {
        miaoLogger().d(
            "name" to method.name,
            "reqMessage" to method.reqMessage
        )
        return suspendCoroutine { continuation ->
            val req = buildRequest()
            client.newCall(req).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val respMessage = parseResponse(response)
                        continuation.resume(respMessage)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }
            })
        }
    }
}
