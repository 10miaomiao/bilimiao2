package com.a10miaomiao.bilimiao.ui.commponents.model

import android.content.Context
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.SettingUtil
import com.a10miaomiao.bilimiao.utils.TimeSettingUtil
import com.a10miaomiao.miaoandriod.binding.MiaoBinding
import java.util.*

class DateModel(val binding: MiaoBinding) {
    var year by binding.miao(2009)
    var month by binding.miao(1)
    var date by binding.miao(1)

    fun now() {
        val now = Date()
        setDate(now)
    }

    fun setValue(value: DateModel) {
        if (value.year != year) year = value.year
        if (value.month != month) month = value.month
        if (value.date != date) date = value.date
    }

    fun getValue(span: String = "") = "$year$span${fillZero(month)}$span${fillZero(date)}"

    fun getDate() = Date(year - 1900, month - 1, date)

    fun setDate(date: Date): DateModel{
        year = date.year + 1900
        month = date.month + 1
        this.date = date.date
        return this
    }

    fun diff(value: DateModel) = when {
        value.year != year -> true
        value.month != month -> true
        value.date != date -> true
        else -> false
    }

    fun save(context: Context, type: String) {
        val time = year.toString() + fillZero(month) + fillZero(date)
        SettingUtil.putString(context, type, time)
    }

    fun read(context: Context, type: String): DateModel {
        val time_type = SettingUtil.getInt(context, ConstantUtil.TIME_TYPE, 0)
        if (time_type == 0) {
            now()
            if (type == ConstantUtil.TIME_FROM) {
                setValue(getTimeByGapCount(-7))
            }
            return this
        }
        val time_str = SettingUtil.getString(context, type, "20180909")
        val lenght = time_str.length
        year = time_str.substring(0, lenght - 4).toInt()
        month = time_str.substring(lenght - 4, lenght - 2).toInt()
        date = time_str.substring(lenght - 2, lenght).toInt()
        return this
    }

    private fun fillZero(i: Int): String {
        return if (i < 10) "0$i" else i.toString()
    }

    /**
     * 根据月份获取 TimeFrom
     */
    fun getTimeFromByMonth(): DateModel {
        return DateModel(binding).let {
            it.year = year
            it.month = month
            it.date = date
            it
        }
    }

    /**
     * 根据月份获取 TimeTo
     */
    fun getTimeToByMonth(): DateModel {
        return DateModel(binding).let {
            it.year = year
            it.month = month
            it.date = TimeSettingUtil.getMonthDate(year, month)
            it
        }
    }

    /**
     * 根据获取gapCount天后的时间
     */
    fun getTimeByGapCount(gapCount: Int): DateModel {
//        val time = DateModel(binding)
//        time.year = year
//        val n = date - gapCount
//        time.month = if (n > 0) month
//        else if (month == 1) {
//            time.year--
//            12
//        } else month - 1
//        time.date = if (n > 0) n
//        else TimeSettingUtil.getMonthDate(time.year, time.month) + n
//        return time
        var calendar = Calendar.getInstance()
        calendar.time = getDate()
        calendar.add(Calendar.DATE, gapCount)//参数-，换为1则为加1代表在原来时间的基础上减少一天一天;ps:加减月数 小时同加减天数
        return DateModel(binding).setDate(calendar.time)
    }

    /**
     * 计算时间间隔
     */
    fun getGapCount(date: DateModel): Int {
        val startL = getDate().time
        val endL = date.getDate().time
        return  ((endL - startL) / (1000 * 60 * 60 * 24)).toInt()
    }

}