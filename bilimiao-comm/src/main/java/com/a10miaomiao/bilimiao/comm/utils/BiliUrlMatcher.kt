package com.a10miaomiao.bilimiao.comm.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.TypedValue
import android.view.View
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.ACTIVITY_HEIGHT_FIXED
import androidx.browser.customtabs.CustomTabsService
import androidx.core.content.ContextCompat
import java.util.Collections
import java.util.regex.Pattern

object BiliUrlMatcher {

    /**
     * 寻找视频id
     * 【你的名字】【煽情对白】N2V - 寻找 UP主: 卷成卷原创电音平台 http://www.bilibili.com/video/av13840208
     * NEW GAME!!, http://bangumi.bilibili.com/anime/6330/
     * 播主：Zelo-Balance http://live.bilibili.com/live/14047.html
     *
     * 【2018年1月新番介绍】童年回忆，京紫霸权？续作第二季 http://www.bilibili.com/read/cv99107
     *
     * 新版本番剧
     * Slow Start, http://m.bilibili.com/bangumi/play/ss21675
     *
     *
     */
    fun findIDByUrl(text: String): Array<String> {
        var a = ""
        a = matchingID(text, ".*://www.bilibili.com/video/av(\\d+)")
        if (a != "") {
            return arrayOf("AV", a)
        }
        a = matchingID(text,".*://m.bilibili.com/bangumi/play/ep(\\d+).*")
        if (a != "") {
            return arrayOf("EP", a)
        }
        a = matchingID(text,".*://www.bilibili.com/bangumi/play/ep(\\d+).*")
        if (a != "") {
            return arrayOf("EP", a)
        }
        a = matchingID(text, ".*://bangumi.bilibili.com/anime/(\\d+)/")
        if (a != "") {
            return arrayOf("SS", a)
        }
        a = matchingID(text, ".*://live.bilibili.com/live/(\\d+).html")
        if (a != "") {
            return arrayOf("ROOM", a)
        }
        a = matchingID(text, ".*://m.bilibili.com/audio/au(\\d+)")
        if (a != "") {
            return arrayOf("AU", a)
        }
        a = matchingID(text, ".*://www.bilibili.com/read/cv(\\d+)")
        if (a != "") {
            return arrayOf("CV", a)
        }
        a = matchingID(text, ".*bilibili.com/read/mobile/(\\d+)")
        if (a != "") {
            return arrayOf("CV", a)
        }
        a = matchingID(text, ".*://m.bilibili.com/bangumi/play/ss(\\d+)")
        if (a != "") {
            return arrayOf("SS", a)
        }
        a = matchingID(text, ".*://live.bilibili.com/(\\d+)")
        if (a != "") {
            return arrayOf("ROOM", a)
        }
        a = matchingID(text, ".*bilibili.com/video/BV([a-zA-Z0-9]+)")
        if (a != "") {
            return arrayOf("BV", a)
        }
        a = matchingID(text, ".*://m.bilibili.com/playlist/pl\\d+\\?bvid=BV([a-zA-Z0-9]+)")
        if (a != "") {
            return arrayOf("BV", a)
        }
        a = matchingID(text, ".*://space.bilibili.com/(\\d+)")
        if (a != "") {
            return arrayOf("UID", a)
        }
        return arrayOf("未知类型", a)
    }



    /**
     * 用正则获取视频id
     */
    fun matchingID(text: String, regex: String): String {
//        regex.toRegex().find(text).
        val compile = Pattern.compile(regex)
        val matcher = compile.matcher(text)
        if (matcher.find())
            return matcher.group(1)//提取匹配到的结果
        return ""
    }

    fun customString(content: String): String{
        var result = "[aA][vV](\\d+)".toRegex().replace(content, "[$0](https://www.bilibili.com/video/av$1)")
        result = "BV([a-zA-Z0-9]+)".toRegex().replace(result, "[$0](https://www.bilibili.com/video/BV$1)")
        return result
    }

    fun toUrlLink (view: View, url: String) {
        toUrlLink(view.context, url)
    }

    fun toUrlLink (context: Context, url: String) {
        val uri = Uri.parse(
            if ("://" in url) {
                url
            } else {
                "http://$url"
            }
        )

//        if (isChromeSupported(context)) {
            val typedValue = TypedValue()
            val attrId = com.google.android.material.R.attr.colorSurfaceVariant
            context.theme.resolveAttribute(attrId, typedValue, true)
            val intent = CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(
                    CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(ContextCompat.getColor(context, typedValue.resourceId))
                        .build()
                )
                .build()
            intent.launchUrl(context, uri)
//        } else {
//            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
//        }
    }

//    private fun isChromeSupported(context: Context): Boolean {
//        val packageName = CustomTabsClient.getPackageName(context, Collections.emptyList())
//        return packageName == null
//    }


}