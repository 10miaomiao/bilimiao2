package com.a10miaomiao.bilimiao.utils

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
}