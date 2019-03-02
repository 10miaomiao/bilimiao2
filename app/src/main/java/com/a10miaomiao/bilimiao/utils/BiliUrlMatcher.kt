package com.a10miaomiao.bilimiao.utils

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
        DebugMiao.log(text)
        var a = ""
        a = matchingID(text, ".*http://www.bilibili.com/video/av(\\d+)")
        if (a != "") {
            return arrayOf("AV", a)
        }
        a = matchingID(text,".*https://m.bilibili.com/bangumi/play/ep(\\d+).*")
        if (a != "") {
            return arrayOf("EP", a)
        }
        a = matchingID(text, ".*http://bangumi.bilibili.com/anime/(\\d+)/")
        if (a != "") {
            return arrayOf("SS", a)
        }
        a = matchingID(text, ".*http://live.bilibili.com/live/(\\d+).html")
        if (a != "") {
            return arrayOf("ROOM", a)
        }
        a = matchingID(text, ".*http://m.bilibili.com/audio/au(\\d+)")
        if (a != "") {
            return arrayOf("CV", a)
        }
        a = matchingID(text, ".*http://www.bilibili.com/read/cv(\\d+)")
        if (a != "") {
            return arrayOf("SS", a)
        }
        a = matchingID(text, ".*http://m.bilibili.com/bangumi/play/ss(\\d+)")
        if (a != "") {
            return arrayOf("SS", a)
        }
        a = matchingID(text, ".*http://live.bilibili.com/(\\d+)")
        if (a != "") {
            return arrayOf("ROOM", a)
        }
        return arrayOf("未知类型", a)
    }

    /**
     * 用正则获取视频id
     */
    fun matchingID(text: String, regex: String): String {
        val compile = Pattern.compile(regex)
        val matcher = compile.matcher(text)
        if (matcher.find())
            return matcher.group(1)//提取匹配到的结果
        return ""
    }


}