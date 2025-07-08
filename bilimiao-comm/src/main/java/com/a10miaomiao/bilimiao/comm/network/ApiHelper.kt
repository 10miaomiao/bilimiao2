package com.a10miaomiao.bilimiao.comm.network

import android.os.Build
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * Created by 10喵喵 on 2017/4/9.
 */
object ApiHelper {

    const val BUILD_VERSION = 1450000
    const val BILI_APP_VERSION = "1.45.0"
    const val MOBI_APP_HD = "android_hd" // 默认HD
    const val MOBI_APP = "android"
    const val STATISTICS_HD = """{"appId":5,"platform":3,"version":"$BILI_APP_VERSION","abtest":""}"""
    const val STATISTICS = """{"appId":1,"platform":3,"version":"7.66.0","abtest":""}"""
    const val PLATFORM = "android"
    const val LOCALE = "zh_CN"
    const val CHANNEL = "bili"

    // 用哪个APP_KEY登录后，之后的请求之后只能用同一个APP_KEY，现统一使用HD版的APP_KEY，APP版的APP_KEY无法使用二维码登录
    // Android APP
    const val APP_KEY = "1d8b6e7d45233436";
    const val APP_SECRET = "560c52ccd288fed045859ed18bffd973"
    // Android HD
    const val APP_KEY_HD = "dfca71928277209b";
    const val APP_SECRET_HD = "b5475a8825547a4fc26c7d518eaaa02e"

    const val REFERER = "https://www.bilibili.com/"
    const val APP_BASE = "https://app.bilibili.com/"
    const val GRPC_BASE = "https://app.bilibili.com/"

    /**
     * User-Agent: Mozilla/5.0 BiliDroid/1.45.0 (bbcallen@gmail.com) os/android model/2201123C mobi_app/android_hd build/1450000 channel/bili innerVer/1450000 osVer/12 network/2
     */
    val USER_AGENT = """
        |Mozilla/5.0 
        |BiliDroid/1.45.0 (bbcallen@gmail.com) 
        |os/android 
        |model/${Build.MODEL} 
        |mobi_app/android_hd 
        |build/${BUILD_VERSION} 
        |channel/bili 
        |innerVer/${BUILD_VERSION} 
        |osVer/${Build.VERSION.RELEASE} 
        |network/2
        """.trimMargin().replace("\n", "")


    fun getTimeSpan() = Date().time / 1000

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
        return params.map {
            if (it.key.isNotBlank() && !it.value.isNullOrBlank()) {
                "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}"
            } else {
                ""
            }
        }.filter {
            it.isNotBlank()
        }.run {
            if (isSort) {
                sorted().joinToString("&")
            } else {
                joinToString("&")
            }
        }
    }

    fun addAccessKeyAndMidToParams(params: MutableMap<String, String?>){
        BilimiaoCommApp.commApp.loginInfo?.token_info?.let{
            params["access_key"] = it.access_token
            if (!params.contains("mid")) {
                params["mid"] = it.mid.toString()
            }
        }
    }

    fun createParams(
        vararg pairs: Pair<String, String?>,
        appKey: String = APP_KEY_HD,
        appSecrer: String = APP_SECRET_HD,
    ): MutableMap<String, String?>{
        return createParams(
            mapOf(*pairs),
            appKey,
            appSecrer
        )
    }
    fun createParams(
        params: Map<String, String?>,
        appKey: String = APP_KEY_HD,
        appSecrer: String = APP_SECRET_HD,
    ): MutableMap<String, String?> {
        var _appSecrer = appSecrer
        val paramMap = mutableMapOf<String, String?>(
            "appkey" to appKey,
//            "buvid" to BilimiaoCommApp.commApp.getBilibiliBuvid(),
            "platform" to PLATFORM,
            "channel" to CHANNEL,
            "mobi_app" to MOBI_APP_HD,
            "statistics" to STATISTICS_HD,
            "build" to BUILD_VERSION.toString(),
            "c_locale" to LOCALE,
            "s_locale" to LOCALE,
            "ts" to getTimeSpan().toString(),
        )
        paramMap.putAll(params)
        if (paramMap["notoken"].isNullOrBlank()) {
            addAccessKeyAndMidToParams(paramMap)
        }
        paramMap["sign"] = getSing(paramMap, _appSecrer)
        return paramMap
    }

}
