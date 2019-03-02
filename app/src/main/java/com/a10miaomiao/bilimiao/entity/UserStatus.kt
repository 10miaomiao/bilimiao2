package com.a10miaomiao.bilimiao.entity

data class UserStatus(
        val follow: Int,
        val pay: Int,
        val sponsor: Int,
        val vip: Int,
        val vip_frozen: Int
)