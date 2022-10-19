package com.a10miaomiao.bilimiao.widget.rangedate.model

data class DayInfo(

    var year: Int = 0, //属于的年份
    var month: Int = 0, //属于的月份
    var day: Int = 0, //日期，几号

    var type: Int = TYPE_DAY_PLACEHOLDER, //类型
    var groupName: String = "", //分组title

    var jr: String? = null, //节日

    var isWeekend: Boolean = false, //是否是周末
    var isEnableDay: Boolean = false, //是否是有效的时间
    var isToday: Boolean = false, //是否是当天
    var isHourMode: Boolean = false, //是否是钟点房模式
    var isSelect: Boolean = false, //是否选中入店时间
    var isMiddle: Boolean = false, //是否是中间的
    var isSelectEnd: Boolean = false, //是否选中离店时间
    var isEnableMorn: Boolean = false //凌晨特殊选择

) {

    companion object {
        const val TYPE_DAY_PLACEHOLDER = 0
        const val TYPE_DAY_TITLE = 1
        const val TYPE_DAY_NORMAL = 2
    }

}
