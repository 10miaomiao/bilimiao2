package com.a10miaomiao.bilimiao.utils

import android.content.Context

object TimeSettingUtil {

    fun setTimeType(context: Context, type: Int) {
        SettingUtil.putInt(context, ConstantUtil.TIME_TYPE, type)
    }

    fun setTimeFrom(context: Context, time: String) {
        SettingUtil.putString(context, ConstantUtil.TIME_FROM, time)
    }

    fun setTimeType(context: Context, time: String) {
        SettingUtil.putString(context, ConstantUtil.TIME_TO, time)
    }

    fun getTimeType(context: Context): Int {
        return SettingUtil.getInt(context, ConstantUtil.TIME_FROM, ConstantUtil.TIME_TYPE_DEFAULT)
    }

    fun getTimeFrom(context: Context): String {
        return SettingUtil.getString(context, ConstantUtil.TIME_FROM, "20180702")
    }

    fun getTimeTo(context: Context): String {
        return SettingUtil.getString(context, ConstantUtil.TIME_TO, "20180709")
    }


    /**
     * 获取某一月份的天数
     */
    fun getMonthDate(isLeapYear: Boolean, month: Int): Int {
        if (month in 1..12) {
            val dates = intArrayOf(31, if (isLeapYear) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            return dates[month - 1]
        }
        return 30

    }

    fun getMonthDate(year: Int, month: Int): Int {
        return getMonthDate(year % 4 == 0, month)
    }
}