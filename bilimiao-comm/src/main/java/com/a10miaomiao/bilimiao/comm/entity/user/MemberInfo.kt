package com.a10miaomiao.bilimiao.comm.entity.user

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MemberInfo(
//        val DisplayRank: String,
    val avatar: String,
//        val fans_detail: Any,
    val following: Int,
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
): Parcelable {
    @Parcelize
    data class LevelInfo(
        val current_exp: Int,
        val current_level: Int,
        val current_min: Int,
        val next_exp: Int
    ): Parcelable

    @Parcelize
    data class Vip(
        val accessStatus: Int,
        val dueRemark: String,
        val vipDueDate: Double,
        val vipStatus: Int,
        val vipStatusWarn: String,
        val vipType: Int
    ): Parcelable
}