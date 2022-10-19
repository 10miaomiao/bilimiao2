package com.a10miaomiao.bilimiao.widget.rangedate.model

enum class SelectDateType(var type: Int) {

    TYPE_NORMAL(1), //正常选择，限定28天
    TYPE_HOUR(2), //钟点房选择
    TYPE_DELAY(3); //延住，在上次离店时间之后，限定28天

    companion object {

        fun findType(value: Int?): SelectDateType? {
            return when (value) {
                TYPE_NORMAL.type -> TYPE_NORMAL
                TYPE_HOUR.type   -> TYPE_HOUR
                TYPE_DELAY.type  -> TYPE_DELAY
                else             -> null
            }
        }
    }

}