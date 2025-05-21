package com.a10miaomiao.bilimiao.comm.entity.message

import kotlinx.serialization.Serializable

@Serializable
data class AtMessageInfo (
    val id: Long,
    /**
     * @的用户.
     */
    val user: MessageUserInfo,

    /**
     * @消息详情.
     */
    val item: ItemInfo,

    /**
     * @时间.
     */
    val at_time: Long
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
        val image: String,
        val uri: String,
        val native_uri: String,
        val source_content: String,
    )
}