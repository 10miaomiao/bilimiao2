package com.a10miaomiao.bilimiao.utils

import java.text.SimpleDateFormat
import java.util.*

object NumberUtil {
    fun converString(num: Int): String {
        if (num < 10000) {
            return num.toString()
        }
        val unit = "万"
        val newNum = num / 10000.0

        val numStr = String.format("%." + 1 + "f", newNum)
        return numStr + unit
    }

    fun converString(num: String): String {
        try {
            return converString(Integer.valueOf(num))
        } catch (e: NumberFormatException) {
            return "--"
        }

    }

    fun converDuration(duration: Int): String {
        var s = (duration % 60).toString()
        var min = (duration / 60).toString()
        if (s.length == 1)
            s = "0$s"
        if (min.length == 1)
            min = "0$min"
        return "$min:$s"
    }

    fun converCTime(ctime: Long): String {
        val date = Date(ctime * 1000)
        val now = Calendar.getInstance().timeInMillis
        val deltime = (now - date.time) / 1000
        return when {
            deltime > 365 * 24 * 60 * 60 -> {
                val sf = SimpleDateFormat("yyyy-MM-dd HH:mm")
                sf.format(date)
            }
            deltime > 24 * 60 * 60 -> (deltime / (24 * 60 * 60)).toInt().toString() + "天前"
            deltime > 60 * 60 -> (deltime / (60 * 60)).toInt().toString() + "小时前"
            deltime > 60 -> (deltime / 60).toInt().toString() + "分钟前"
            else -> deltime.toString() + "秒前"
        }
    }
}