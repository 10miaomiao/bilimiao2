package com.a10miaomiao.bilimiao.netword

import com.a10miaomiao.bilimiao.utils.DebugMiao
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.ArrayList
import java.util.Collections
import java.util.Date
import java.util.List

/**
 * Created by 10喵喵 on 2017/4/9.
 */
object ApiHelper {
    val appKey_IOS = "4ebafd7c4951b366";
    val appKey_Android = "c1b107428d337928";
    val appkey_DONTNOT = "85eb6835b0a1034e";//e5b8ba95cab6104100be35739304c23a

    val _appSecret_Wp = "ba3a4e554e9a6e15dc4d1d70c2b154e3";//Wp
    val _appSecret_IOS = "8cb98205e9b2ad3669aad0fce12a4c13";//Ios
    val _appSecret_Android = "ea85624dfcf12d7cc7b2b3a94fac1f2c";//Android
    val _appSecret_DONTNOT = "2ad42749773c441109bdc0191257a664";
    val _appSecret_Android2 = "jr3fcr8w7qey8wb0ty5bofurg2cmad8x";
    val _appSecret_VIP = "jr3fcr8w7qey8wb0ty5bofurg2cmad8x";

    // Get from bilibili android client
    val APP_KEY_NEW = "1d8b6e7d45233436"
    val APP_SECRET_NEW = "560c52ccd288fed045859ed18bffd973"

    fun getTimeSpen() = Date().time


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
}
