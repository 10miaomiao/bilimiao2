package com.a10miaomiao.bilimiao.widget.rangedate

import android.content.Context
import com.a10miaomiao.bilimiao.widget.rangedate.model.DayInfo
import com.a10miaomiao.bilimiao.widget.rangedate.model.RoomType
import com.a10miaomiao.bilimiao.widget.rangedate.model.SelectDateInfo
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author DarklyCoder
 * @Description
 * @date 2018/10/11
 */
class RangeDateUtils {

    companion object {

        fun sp2px(context: Context, spValue: Float): Int {
            val fontScale = context.resources.displayMetrics.scaledDensity
            return (spValue * fontScale + 0.5f).toInt()
        }

        fun dp2px(context: Context?, dpValue: Int): Float {
            if (null == context || dpValue == 0) {
                return dpValue.toFloat()
            }

            val scale = context.resources.displayMetrics.density
            return dpValue * scale + 0.5f
        }

        /**
         * 获取节日描述
         */
        fun getJR(month: Int, day: Int): String {
            if (month == 1 && day == 1) {
                return "元旦"

            } else if (month == 2 && day == 14) {
                return "情人节"

            } else if (month == 4 && day == 5) {
                return "清明节"

            } else if (month == 5 && day == 1) {
                return "劳动节"

            } else if (month == 6 && day == 1) {
                return "儿童节"

            } else if (month == 7 && day == 1) {
                return "建党节"

            } else if (month == 8 && day == 1) {
                return "建军节"

            } else if (month == 9 && day == 10) {
                return "教师节"

            } else if (month == 10 && day == 1) {
                return "国庆"

            } else if (month == 11 && day == 11) {
                return "双11"

            } else if (month == 12 && day == 24) {
                return "平安夜"

            } else if (month == 12 && day == 25) {
                return "圣诞节"
            }

            return ""
        }

        /**
         * 是否是今天
         */
        fun isToday(year: Int, mouth: Int, day: Int): Boolean {
            val calendar = Calendar.getInstance()

            val curYear = calendar.get(Calendar.YEAR)
            val curMonth = calendar.get(Calendar.MONTH) + 1
            val curDay = calendar.get(Calendar.DAY_OF_MONTH)

            return curYear == year && curMonth == mouth && curDay == day
        }

        /**
         * 是否是周末
         */
        fun isWeekend(year: Int, mouth: Int, day: Int): Boolean {
            if (year <= 0 || mouth <= 0 || day <= 0) {
                return false
            }

            val calendar = Calendar.getInstance()
            calendar.set(year, mouth - 1, day)

            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            return dayOfWeek == 1 || dayOfWeek == 7
        }

        /**
         * 是否是有效的时间
         */
        fun isValidDay(dayInfo: DayInfo): Boolean {
            val calendar = Calendar.getInstance()
            val yearStart = calendar.get(Calendar.YEAR)
            val monthStart = calendar.get(Calendar.MONTH) + 1
            val dayStart = calendar.get(Calendar.DAY_OF_MONTH)
            val dayInfoStart = DayInfo(yearStart, monthStart, dayStart)

            val calendarEnd = Calendar.getInstance()
            calendarEnd.add(Calendar.DAY_OF_YEAR, 365)
            val yearEnd = calendarEnd.get(Calendar.YEAR)
            val monthEnd = calendarEnd.get(Calendar.MONTH) + 1
            val dayEnd = calendarEnd.get(Calendar.DAY_OF_MONTH)
            val dayInfoEnd = DayInfo(yearEnd, monthEnd, dayEnd)

            //判断是否在有效期内
            if (isSameDay(dayInfo, dayInfoStart) || isSameDay(dayInfo, dayInfoEnd)) {
                return true
            }

            return isBigThanFirstDay(dayInfoStart, dayInfo) && isBigThanFirstDay(dayInfo, dayInfoEnd)
        }

        /**
         * 是否是相同的一天
         */
        fun isSameDay(day1: DayInfo?, day2: DayInfo?): Boolean {
            if (null == day1 || null == day2) {
                return false
            }

            return day1.year == day2.year && day1.month == day2.month && day1.day == day2.day
        }

        /**
         * 是否比选择的第一个时间大
         */
        fun isBigThanFirstDay(firstDay: DayInfo?, day: DayInfo?): Boolean {
            if (null == firstDay || null == day) {
                return false
            }

            val dexYear = day.year - firstDay.year
            val dexMouth = day.month - firstDay.month
            val dexDay = day.day - firstDay.day

            return when {
                dexYear != 0 -> return dexYear > 0

                else         -> {
                    when {
                        dexMouth != 0 -> return dexMouth > 0

                        else          -> {
                            when {
                                dexDay != 0 -> return dexDay > 0

                                else        -> false
                            }
                        }
                    }
                }
            }
        }

        /**
         * 是否在指定区间范围内，不包含边界
         */
        fun isInDayRange(day: DayInfo?, firstDay: DayInfo?, endDay: DayInfo?): Boolean {
            return isBigThanFirstDay(firstDay, day) && isBigThanFirstDay(day, endDay)
        }

        private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE)

        /**
         * 获取时间
         */
        fun getTime(day: DayInfo): Long {
            return sdf.parse("${day.year}-${day.month}-${day.day}").time
        }

        private val sdfMd = SimpleDateFormat("MM月dd号", Locale.CHINESE)

        /**
         * 获取月日显示
         */
        fun getMD(time: Long): String {
            return sdfMd.format(Date(time))
        }

        fun getDayInfo(time: Long): DayInfo {
            val calendar = Calendar.getInstance()
            calendar.time = Date(time)
            val year = calendar.get(Calendar.YEAR)
            val mouth = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            return DayInfo(year, mouth, day)
        }

        /**
         * 获取默认日期选择
         */
        fun getDefaultSelectDate(): SelectDateInfo {
            val calendar = Calendar.getInstance()
            val yearStart = calendar.get(Calendar.YEAR)
            val monthStart = calendar.get(Calendar.MONTH) + 1
            val dayStart = calendar.get(Calendar.DAY_OF_MONTH)
            val dayInfoStart = DayInfo(yearStart, monthStart, dayStart)

            val calendarEnd = Calendar.getInstance()
            calendarEnd.add(Calendar.DATE, 1)
            val yearEnd = calendarEnd.get(Calendar.YEAR)
            val monthEnd = calendarEnd.get(Calendar.MONTH) + 1
            val dayEnd = calendarEnd.get(Calendar.DAY_OF_MONTH)
            val dayInfoEnd = DayInfo(yearEnd, monthEnd, dayEnd)

            return SelectDateInfo(
                getTime(dayInfoStart),
                getTime(dayInfoEnd),
                1,
                RoomType.TYPE_ROOM_NORMAL.type,
                getTime(dayInfoStart)
            )
        }

    }

}