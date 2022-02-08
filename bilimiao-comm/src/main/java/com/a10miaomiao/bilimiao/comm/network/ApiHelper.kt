package com.a10miaomiao.bilimiao.comm.network

import android.net.Uri
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Date

/**
 * Created by 10喵喵 on 2017/4/9.
 */
object ApiHelper {
    val appKey_Android = "c1b107428d337928";
    val _appSecret_Android = "ea85624dfcf12d7cc7b2b3a94fac1f2c";//Android

    val APP_KEY_NEW = "1d8b6e7d45233436"
    val APP_SECRET_NEW = "560c52ccd288fed045859ed18bffd973"

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

    fun getAndroidSign(url: String): String {
        return getSing(url, _appSecret_Android)
    }

    fun getNewSign(url: String): String {
        return getSing(url, APP_SECRET_NEW)
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

    fun getSing(params: Map<String, String>, secret: String): String {
        val list = params.map { "${it.key}=${Uri.encode(it.value)}" }.toMutableList()
        list.sort()
        return with(StringBuilder()) {
            list.forEach { item ->
                append(if (isNotEmpty()) "&" else "")
                append(item)
            }
            return@with getMD5(toString() + secret)
        }
    }

    fun urlencode(params: Map<String, String>): String {
        val stringBuilder = StringBuilder()
        var i = 0
        params.keys.forEach { key ->
            if (i == 0){
                stringBuilder.append("$key=${Uri.encode(params[key])}")
            }else{
                stringBuilder.append("&$key=${Uri.encode(params[key])}")
            }
            i++
        }
        return stringBuilder.toString()
    }

    fun addAccessKeyAndMidToParams(params: MutableMap<String, String>){
        BilimiaoCommApp.commApp.loginInfo?.token_info?.let{
            params["access_key"] = it.access_token
            params["mid"] = it.mid.toString()
        }
    }

    fun createParams(vararg pairs: Pair<String, String>): MutableMap<String, String>{
        val params = mutableMapOf(
            *pairs,
            "appkey" to APP_KEY_NEW,
            "build" to "5340000",
            "mobi_app" to "android",
            "platform" to "android",
            "ts" to getTimeSpen().toString()
        )
        addAccessKeyAndMidToParams(params)
        params["sign"] = getSing(params, APP_SECRET_NEW)
        return params
    }


}
