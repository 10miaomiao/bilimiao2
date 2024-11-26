package com.a10miaomiao.bilimiao.comm.store.model


import java.util.*

class DateModel() {
    var year = 2009
    var month = 1
    var date = 1

    fun setValue(str: String) {
        val lenght = str.length
        year = str.substring(0, lenght - 4).toInt()
        month = str.substring(lenght - 4, lenght - 2).toInt()
        date = str.substring(lenght - 2, lenght).toInt()
    }

    fun getValue(span: String = "") = "$year$span${fillZero(month)}$span${fillZero(date)}"

    fun getDate() = Date(year - 1900, month - 1, date)

    fun setDate(date: Date): DateModel {
        year = date.year + 1900
        month = date.month + 1
        this.date = date.date
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (other is DateModel
            && other.year == year
            && other.month == month
            && other.date == date) {
            return true
        }
        return false
    }

//        fun save(context: Context, type: String) {
//            val time = year.toString() + fillZero(month) + fillZero(date)
//            SettingUtil.putString(context, type, time)
//        }

    private fun fillZero(i: Int): String {
        return if (i < 10) "0$i" else i.toString()
    }

    /**
     * 根据月份获取 TimeFrom
     */
    fun getTimeFromByMonth(): DateModel {
        return DateModel().let {
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
        return DateModel().let {
            it.year = year
            it.month = month
            it.date = getMonthDate()
            it
        }
    }

    /**
     * 获取某一月份的天数
     */
    private fun getMonthDate(isLeapYear: Boolean, month: Int): Int {
        if (month in 1..12) {
            val dates = intArrayOf(31, if (isLeapYear) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            return dates[month - 1]
        }
        return 30

    }

    fun getMonthDate(): Int {
        return getMonthDate(year % 4 == 0, month)
    }

    /**
     * 根据获取gapCount天后的时间
     */
    fun getTimeByGapCount(gapCount: Int): DateModel {
        var calendar = Calendar.getInstance()
        calendar.time = getDate()
        calendar.add(Calendar.DATE, gapCount)//参数-，换为1则为加1代表在原来时间的基础上减少一天一天;ps:加减月数 小时同加减天数
        return DateModel().setDate(calendar.time)
    }

    /**
     * 计算时间间隔
     */
    fun getGapCount(date: DateModel): Int {
        val startL = getDate().time
        val endL = date.getDate().time
        return ((endL - startL) / (1000 * 60 * 60 * 24)).toInt()
    }

    fun set(newDate: DateModel) {
        year = newDate.year
        month = newDate.month
        date = newDate.date
    }

    fun copy(): DateModel {
        return DateModel().let {
            it.year = year
            it.month = month
            it.date = date
            it
        }
    }

}