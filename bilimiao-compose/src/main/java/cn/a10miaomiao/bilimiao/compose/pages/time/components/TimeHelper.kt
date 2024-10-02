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