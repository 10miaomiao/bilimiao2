package cn.a10miaomiao.bilimiao.compose.pages.time.components

/**
 * 计算星期几
 */
internal fun getWeek(y: Int, m: Int, d: Int): Int {
    var y = y
    var m = m
    if (m < 3) {
        m += 12
        --y
    }
    return (d + 1 + 2 * m + 3 * (m + 1) / 5 + y + (y shr 2) - y / 100 + y / 400) % 7
}

/**
 * 计算某月 1 号在「周一开头」日历中的列偏移（0 = 周一）。
 *
 * [getWeek] 返回的是「周日开头」的星期值，这里换算成周一开头，
 * 与日历表头的「一 … 日」顺序保持一致。
 */
internal fun getMonthStartOffset(y: Int, m: Int): Int = (getWeek(y, m, 1) + 6) % 7

/**
 * 是否闰年
 */
internal fun isLeapYear(y: Int): Boolean {
    return (y % 4 == 0 && y % 100 != 0) || y % 400 == 0
}

/**
 * 计算一个月有多少天
 */
internal fun getMonthDayNum(y: Int, m: Int): Int {
    if (m in 1..12) {
        val dates = intArrayOf(31, if (isLeapYear(y)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        return dates[m - 1]
    }
    return 30
}