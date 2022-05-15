package com.a10miaomiao.bilimiao.comm.network

import android.net.Uri
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Date

/**
 * Created by 10喵喵 on 2017/4/9.
 */
object ApiHelper {

    val BUILD_VERSION = 5520400
    val APP_KEY = "4409e2ce8ffd12b8";
    val APP_SECRET = "59b43e04ad6965f34319062b478f83dd"

    fun getTimeSpen() = Date().time / 1000


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
            params["mid"] = it.mid.toString()
        }
    }

    fun createParams(vararg pairs: Pair<String, String?>): MutableMap<String, String?>{
        val params = mutableMapOf(
            *pairs,
            "appkey" to APP_KEY,
            "build" to BUILD_VERSION.toString(),
            "mobi_app" to "android",
            "platform" to "android",
            "ts" to getTimeSpen().toString()
        )
        addAccessKeyAndMidToParams(params)
        params["sign"] = getSing(params, APP_SECRET)
        return params
    }

}
