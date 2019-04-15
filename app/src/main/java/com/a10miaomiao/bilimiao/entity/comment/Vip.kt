package com.a10miaomiao.bilimiao.entity.comment

data class Vip(
        val accessStatus: Int,
        val dueRemark: String,
        val vipDueDate: Long,
        val vipStatus: Int,
        val vipStatusWarn: String,
        val vipType: Int
)