package com.a10miaomiao.bilimiao.comm.entity.user

data class UserEmotePackageInfo(
    val id: Int,
    val text: String,
    val url: String,
    val mtime: Long,
    val type: Int,
    val attr: Int,
    val meta: MetaInfo,
    val emote: List<UserEmoteInfo>?,
    val flags: FlagInfo,
//    val label: String,
    val package_sub_title: String,
    val ref_mid: String,
) {

    data class MetaInfo(
        val size: Int,
        val item_id: Int,
    )
    data class FlagInfo(
        val added: Boolean,
        val preview: Boolean,
    )
}
