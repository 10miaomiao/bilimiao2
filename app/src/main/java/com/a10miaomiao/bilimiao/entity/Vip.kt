package com.a10miaomiao.bilimiao.entity

data class Vip(
    val accessStatus: Int,
    val dueRemark: String,
    val vipDueDate: Double,
    val vipStatus: Int,
    val vipStatusWarn: String,
    val vipType: Int
)