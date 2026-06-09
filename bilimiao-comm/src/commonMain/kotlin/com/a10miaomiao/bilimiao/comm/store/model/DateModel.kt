package com.a10miaomiao.bilimiao.comm.store.model


import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

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

    fun toLocalDate() = LocalDate(year, month, date)

    fun setDate(date: LocalDate): DateModel {
        year = date.year
        month = date.monthNumber
        this.date = date.dayOfMonth
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
        val newDate = toLocalDate().plus(gapCount, DateTimeUnit.DAY)
        return DateModel().setDate(newDate)
    }

    /**
     * 计算时间间隔
     */
    fun getGapCount(date: DateModel): Int {
        return (date.toLocalDate().toEpochDays() - toLocalDate().toEpochDays()).toInt()
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