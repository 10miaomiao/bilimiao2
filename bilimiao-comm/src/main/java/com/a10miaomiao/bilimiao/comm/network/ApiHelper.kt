package com.a10miaomiao.bilimiao.comm.network

import android.net.Uri
import android.os.Build
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * Created by 10喵喵 on 2017/4/9.
 */
object ApiHelper {

    const val BUILD_VERSION = 1390002
    const val BILI_APP_VERSION = "1.39.0"

    // 用哪个APP_KEY登录后，之后的请求之后只能用同一个APP_KEY，现统一使用HD版的APP_KEY，APP版的APP_KEY无法使用二维码登录
    // Android APP
//    const val APP_KEY = "1d8b6e7d45233436";
//    const val APP_SECRET = "560c52ccd288fed045859ed18bffd973"
    // Android HD
    const val APP_KEY = "dfca71928277209b";
    const val APP_SECRET = "b5475a8825547a4fc26c7d518eaaa02e"

    const val REFERER = "https://www.bilibili.com/"
    const val APP_BASE = "https://app.bilibili.com/"
    const val GRPC_BASE = "https://grpc.biliapi.net/"

    /**
     * User-Agent: Dalvik/2.1.0 (Linux; U; Android 12; sdk_gpc_x86_64 Build/SE2B.220326.023) 1.39.0 os/android model/sdk_gpc_x86_64 mobi_app/android_hd build/1390002 channel/yingyongbao innerVer/1390002 osVer/12 network/1
     *             Dalvik/2.1.0 (Linux; U; Android 12; sdk_gpc_x86_64 Build/SE2B.220326.023) 1.39.0 os/android model/sdk_gpc_x86_64 mobi_app/android_hd build/1390002 channel/bili innerVer/1390002osVer/12 network/2
     */
    val USER_AGENT = """
            |${BiliGRPCConfig.getSystemUserAgent()} 
            |${BILI_APP_VERSION} os/android model/${Build.MODEL} mobi_app/android_hd 
            |build/${BUILD_VERSION} channel/bili innerVer/${BUILD_VERSION}
            |osVer/${Build.VERSION.RELEASE} network/2
        """.trimMargin().replace("\n", "")


    fun getTimeSpen() = Date().time / 1000

    /**
     * 获得一个UUID
     * @return String UUID
     */
    fun getUUID(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    fun generateBuvid(): String {
        val uuid = getUUID() + getUUID()
        return "XY" + uuid.substring(0, 35).uppercase()
    }


    fun getSing(url: String, secret: String): String {
        var str = url.substring(url.indexOf("?", 4) + 1)
        val list = str.split("&").toMutableList()
        list.sort()
        str = with(StringBuilder()) {
            list.forEach { item ->
                append(if (isNotEmpty()) "&" else "")
                append(item)
            }
            return@with toString()
        }
        return getMD5(str + secret)
    }

    fun getMD5(info: String): String {
        try {
            val md5 = MessageDigest.getInstance("MD5")
            md5.update(info.toByteArray())
            val encryption = md5.digest()
            val strBuf = StringBuffer()
            for (i in 0 until encryption.size) {
                if (Integer.toHexString(0xff and encryption[i].toInt()).length == 1) {
                    strBuf.append("0").append(Integer.toHexString(0xff and encryption[i].toInt()))
                } else {
                    strBuf.append(Integer.toHexString(0xff and encryption[i].toInt()))
                }
            }
            return strBuf.toString()
        } catch (e: NoSuchAlgorithmException) {
            return ""
        } catch (e: UnsupportedEncodingException) {
            return ""
        }
    }

    fun getSing(params: Map<String, String?>, secret: String): String {
        val paramsStr = urlencode(params, true)
        return getMD5(paramsStr + secret)
    }

    fun urlencode(params: Map<String, String?>, isSort: Boolean = false): String {
        val list = params.map {
            if (it.value != null) {
                "${Uri.encode(it.key)}=${Uri.encode(it.value)}"
            } else {
                ""
            }
        }.toMutableList()
        if (isSort) {
            list.sort()
        }

        return StringBuilder().apply {
            list.forEach { item ->
                if (item.isNotEmpty()) {
                    append(if (isNotEmpty()) "&" else "")
                    append(item)
                }
            }
        }.toString()
    }

    fun addAccessKeyAndMidToParams(params: MutableMap<String, String?>){
        BilimiaoCommApp.commApp.loginInfo?.token_info?.let{
            params["access_key"] = it.access_token
            if (!params.contains("mid")) {
                params["mid"] = it.mid.toString()
            }
        }
    }

    fun createParams(vararg pairs: Pair<String, String?>): MutableMap<String, String?>{
        val params = mutableMapOf(
            *pairs,
            "appkey" to APP_KEY,
            "build" to BUILD_VERSION.toString(),
            "buvid" to BilimiaoCommApp.commApp.getBilibiliBuvid(),
            "mobi_app" to "android",
            "platform" to "android",
            "ts" to getTimeSpen().toString()
        )
        if (params["notoken"]?.isEmpty() != false) {
            addAccessKeyAndMidToParams(params)
        }
        params["sign"] = getSing(params, APP_SECRET)
        return params
    }

}
