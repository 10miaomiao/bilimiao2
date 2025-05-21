package com.a10miaomiao.bilimiao.comm.entity.message

import kotlinx.serialization.Serializable

@Serializable
data class ReplyMessageInfo (
    val id: Long,
    /**
     * 评论的用户.
     */
    val user: MessageUserInfo,

    /**
     * 回复消息详情.
     */
    val item: ItemInfo,

    /**
     * 点赞数.
     */
    val counts: Int,

    /**
     * 是否为多个回复.
     */
    val is_multi: Int,

    /**
     * 回复时间.
     */
    val reply_time: Long
) {
    @Serializable
    data class ItemInfo(
        val subject_id: Long,
        val root_id: Long,
        val source_id: Long,
        val target_id: Long,
        val type: String,
        val business_id: Int,
        val business: String,
        val title: String,
        val desc: String,
        val image: String,
        val uri: String,
        val native_uri: String,
        val detail_title: String,
        val root_reply_content: String,
        val source_content: String,
        val target_reply_content: String,
        val like_state: String,
    )
}