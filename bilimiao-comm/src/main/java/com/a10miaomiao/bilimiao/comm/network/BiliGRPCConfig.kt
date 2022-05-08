package com.a10miaomiao.bilimiao.comm.network

import android.os.Build
import android.util.Base64
import android.webkit.WebSettings
import bilibili.metadata.MetadataOuterClass
import bilibili.metadata.device.DeviceOuterClass
import bilibili.metadata.fawkes.Fawkes
import bilibili.metadata.locale.LocaleOuterClass
import bilibili.metadata.network.NetworkOuterClass
import bilibili.metadata.restriction.RestrictionOuterClass
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp

object BiliGRPCConfig {

    /**
     * 构建标识.
     */
    val build = 6710300;

    /**
     * 频道.
     */
    val channel = "bilibili140";

    /**
     * 网络状况.
     */
    val networkType = 2;

    /**
     * 未知.
     */
    val networkTF = 0;

    /**
     * 未知.
     */
    val networkOid = "46007";

    /**
     * 未知.
     */
    val buvid = "XZFD48CFF1E68E637D0DF11A562468A8DC314";

    /**
     * 应用类型.
     */
    val mobileApp = "android";

    /**
     * 移动平台.
     */
    val platform = "android";

    /**
     * 产品环境.
     */
    val envorienment = "prod";

    /**
     * 应用Id.
     */
    var appId = 1;

    /**
     * 国家或地区.
     */
    val region = "CN";

    /**
     * 语言.
     */
    val language = "zh";

    /**
     * 获取客户端在Fawkes系统中的信息标头.
     */
    fun getFawkesreqBin(): String {
        val msg = Fawkes.FawkesReq.newBuilder()
            .setAppkey(mobileApp)
            .setEnv(envorienment)
            .build()
        return toBase64(msg.toByteArray())
    }

    /**
     * 获取元数据标头.
     */
    fun getMetadataBin(accessToken: String): String {
        val msg = MetadataOuterClass.Metadata.newBuilder()
            .setAccessKey(accessToken)
            .setMobiApp(mobileApp)
            .setBuild(build)
            .setChannel(channel)
            .setBuvid(buvid)
            .setPlatform(platform)
            .build()
        return toBase64(msg.toByteArray())
    }

    /**
     * 获取设备标头.
     */
    fun getDeviceBin(): String {
        val msg = DeviceOuterClass.Device.newBuilder()
            .setAppId(appId)
            .setMobiApp(mobileApp)
            .setBuild(build)
            .setChannel(channel)
            .setBuvid(buvid)
            .setPlatform(platform)
            .setBrand(Build.BRAND)
            .setModel(Build.MODEL)
            .setOsver(Build.VERSION.RELEASE)
            .build()
        return toBase64(msg.toByteArray())
    }

    /**
     * 获取网络标头.
     */
    fun getNetworkBin(): String {
        val msg = NetworkOuterClass.Network.newBuilder()
            .setType(NetworkOuterClass.NetworkType.WIFI)
            .setOid(networkOid)
            .build()
        return toBase64(msg.toByteArray())
    }

    /**
     * 获取限制标头.
     */
    fun getRestrictionBin(): String {
        val msg = RestrictionOuterClass.Restriction.newBuilder()
            .build()
        return toBase64(msg.toByteArray())
    }

    /**
     * 获取本地化标头.
     */
    fun getLocaleBin(): String {
        val cLocale = LocaleOuterClass.LocaleIds.newBuilder()
            .setLanguage(language)
            .setRegion(region)
            .build()
        val sLocale = LocaleOuterClass.LocaleIds.newBuilder()
            .setLanguage(language)
            .setRegion(region)
            .build()
        val msg = LocaleOuterClass.Locale.newBuilder()
            .setCLocale(cLocale)
            .setSLocale(sLocale)
            .build()
        return toBase64(msg.toByteArray())
    }

    fun getSystemUserAgent(): String {
        var userAgent = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                userAgent = WebSettings.getDefaultUserAgent(BilimiaoCommApp.commApp.app)
            } catch (e: Exception) {
                userAgent = System.getProperty("http.agent")
            }
        } else {
            userAgent = System.getProperty("http.agent")
        }
        userAgent = System.getProperty("http.agent")
        //调整编码，防止中文出错
        val sb = StringBuffer()
        var i = 0
        val length = userAgent.length
        while (i < length) {
            val c = userAgent[i]
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(String.format("\\u%04x", c.toInt()))
            } else {
                sb.append(c)
            }
            i++
        }
        return sb.toString()
    }

    /**
     * 将数据转换为Base64字符串.
     */
    fun toBase64(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.NO_PADDING or Base64.NO_WRAP)
    }
}