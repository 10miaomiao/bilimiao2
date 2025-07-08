package com.a10miaomiao.bilimiao.comm.network

import android.os.Build
import android.util.Base64
import android.webkit.WebSettings
import bilibili.metadata.Metadata
import bilibili.metadata.device.Device
import bilibili.metadata.fawkes.FawkesReq
import bilibili.metadata.locale.Locale
import bilibili.metadata.locale.LocaleIds
import bilibili.metadata.network.Network
import bilibili.metadata.network.NetworkType
import bilibili.metadata.restriction.Restriction
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import pbandk.decodeFromByteArray
import pbandk.encodeToByteArray

object BiliGRPCConfig {

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
//    val buvid = "XZFD48CFF1E68E637D0DF11A562468A8DC314";
    val buvid get() = BilimiaoCommApp.commApp.getBilibiliBuvid()

    /**
     * 应用类型.
     */
    val mobileApp = "android_hd";

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
     * 1为手机安卓APP，5为安卓平板APP
     */
    var appId = 5;

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
        val msg = FawkesReq(
            appkey = mobileApp,
            env = envorienment,
        )
        return toBase64(msg.encodeToByteArray())
    }

    /**
     * 获取元数据标头.
     */
    fun getMetadataBin(accessToken: String): String {
        val msg = Metadata(
            accessKey = accessToken,
            mobiApp = mobileApp,
            build = ApiHelper.BUILD_VERSION,
            channel = channel,
            buvid = buvid,
            platform = platform,
        )
        return toBase64(msg.encodeToByteArray())
    }

    /**
     * 获取设备标头.
     */
    fun getDeviceBin(): String {
        val msg = Device(
            appId = appId,
            mobiApp = mobileApp,
            build = ApiHelper.BUILD_VERSION,
            channel = channel,
            buvid = buvid,
            platform = platform,
            brand = Build.BRAND,
            model = Build.MODEL,
            osver = Build.VERSION.RELEASE,
        )
        return toBase64(msg.encodeToByteArray())
    }

    /**
     * 获取网络标头.
     */
    fun getNetworkBin(): String {
        val msg = Network(
            type = NetworkType.WIFI,
            oid = networkOid,
        )
        return toBase64(msg.encodeToByteArray())
    }

    /**
     * 获取限制标头.
     */
    fun getRestrictionBin(): String {
        val msg = Restriction()
        return toBase64(msg.encodeToByteArray())
    }

    /**
     * 获取本地化标头.
     */
    fun getLocaleBin(): String {
        val cLocale = LocaleIds(
            language = language,
            region = region,
        )
        val sLocale = LocaleIds(
            language = language,
            region = region,
        )
        val msg = Locale(
            cLocale = cLocale,
            sLocale = sLocale,
        )
        return toBase64(msg.encodeToByteArray())
    }

    /**
     *  Dalvik/2.1.0 (Linux; U; Android 12; 2201123C Build/V417IR) 1.45.0 os/android model/2201123C mobi_app/android_hd build/1450000 channel/bili innerVer/1450000 osVer/12 network/2
     */
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