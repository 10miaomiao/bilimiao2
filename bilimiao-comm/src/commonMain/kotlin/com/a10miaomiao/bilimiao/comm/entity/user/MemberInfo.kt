package com.a10miaomiao.bilimiao.comm.entity.user

import kotlinx.serialization.Serializable

@Serializable
data class MemberInfo(
//        val DisplayRank: String,
    val avatar: String,
//        val fans_detail: Any,
    val following: Int = 0,
    val level_info: LevelInfo,
    val mid: String,
//        val nameplate: Nameplate,
//        val official_verify: OfficialVerify,
//        val pendant: Pendant,
    val rank: String,
    val sex: String,
    val sign: String,
    val uname: String,
    val vip: Vip
) {
    @Serializable
    data class LevelInfo(
        val current_exp: Int,
        val current_level: Int,
        val current_min: Int,
        val next_exp: Int
    )

    @Serializable
    data class Vip(
        val accessStatus: Int,
        val dueRemark: String,
        val vipDueDate: Double,
        val vipStatus: Int,
        val vipStatusWarn: String,
        val vipType: Int
    )
}