package com.a10miaomiao.bilimiao.entity

data class ElecSet(
    val elec_list: List<ElecX>,
    val elec_theme: Int,
    val integrity_rate: Int,
    val rmb_rate: Int,
    val round_mode: Int
)