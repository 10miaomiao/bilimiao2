package com.a10miaomiao.bilimiao.comm.utils

import java.text.SimpleDateFormat
import java.util.*

object NumberUtil {
    fun converString(num: Long): String {
        if (num < 10000) {
            return num.toString()
        }
        var unit = "万"
        var newNum = num / 10000.0
        if (num > 9999_9999){
            unit = "亿"
            newNum = num / 10000_0000.0
        }
        val numStr = String.format("%." + 1 + "f", newNum)
        return numStr + unit
    }

    fun converString(num: Int): String {
        if (num < 10000) {
            return num.toString()
        }
        var unit = "万"
        var newNum = num / 10000.0
        if (num > 9999_9999){
            unit = "亿"
            newNum = num / 10000_0000.0
        }
        val numStr = String.format("%." + 1 + "f", newNum)
        return numStr + unit
    }

    fun converString(num: String): String {
        try {
            return converString(Integer.valueOf(num))
        } catch (e: NumberFormatException) {
            return num
        }
    }

    fun converStringOrNull(num: Long?): String? {
        if (num == null) return null
        return converString(num)
    }

    fun converStringOrNull(num: Int?): String? {
        if (num == null) return null
        return converString(num)
    }

    fun converStringOrNull(num: String?): String? {
        if (num == null) return null
        return converString(num)
    }


    fun converDuration(duration: Long): String {
        var s = (duration % 60).toString()
        var min = (duration / 60).toString()
        if (s.length == 1)
            s = "0$s"
        if (min.length == 1)
            min = "0$min"
        return "$min:$s"
    }

    fun converDuration(duration: String): String {
        return try {
            converDuration(Integer.valueOf(duration))
        } catch (e: NumberFormatException) {
            duration
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

    fun converCTime(ctime: Long?): String {
        if (ctime == null) {
            return ""
        }
        val date = Date(ctime * 1000)
        val now = Calendar.getInstance().timeInMillis
        val deltime = (now - date.time) / 1000
        return when {
            deltime > 30 * 24 * 60 * 60 -> {
                val sf = SimpleDateFormat("yyyy-MM-dd HH:mm")
                sf.format(date)
            }
            deltime > 24 * 60 * 60 -> (deltime / (24 * 60 * 60)).toInt().toString() + "天前"
            deltime > 60 * 60 -> (deltime / (60 * 60)).toInt().toString() + "小时前"
            deltime > 60 -> (deltime / 60).toInt().toString() + "分钟前"
            else -> deltime.toString() + "秒前"
        }
    }

    fun isNumber(text: String): Boolean {
        return text.matches(Regex("^[0-9]+$"))
    }
}