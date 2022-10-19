package com.a10miaomiao.bilimiao.widget.rangedate.model

enum class RoomType(var type: Int) {

    TYPE_ROOM_NORMAL(1), //酒店
    TYPE_ROOM_HOUR(2); //钟点房

    companion object {

        fun findType(type: Int?): RoomType? {
            return when (type) {
                TYPE_ROOM_NORMAL.type -> TYPE_ROOM_NORMAL
                TYPE_ROOM_HOUR.type   -> TYPE_ROOM_HOUR
                else                  -> null
            }
        }
    }

}