package com.a10miaomiao.bilimiao.comm.entity.message

import kotlinx.serialization.Serializable

@Serializable
data class LikeMessageInfo(
    val id: String,
    val users: List<MessageUserInfo>,
    val item: ItemInfo,
    val counts: Int,
    val like_time: Long,
    val notice_state: Int,
) {
    @Serializable
    data class ItemInfo(
        val item_id: Long,
        val pid: Int,
        val type: String,
        val business: String,
        val business_id: Long,
        val reply_business_id: Long,
        val like_business_id: Long,
        val title: String,
        val desc: String,
        val image: String,
        val uri: String,
        val detail_name: String,
        val native_uri: String,
        val ctime: Long,
    )
}