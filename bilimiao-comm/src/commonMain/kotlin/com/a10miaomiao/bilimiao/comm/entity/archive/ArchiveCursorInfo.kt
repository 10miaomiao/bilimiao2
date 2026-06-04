package com.a10miaomiao.bilimiao.comm.entity.archive

import kotlinx.serialization.Serializable

@Serializable
data class ArchiveCursorInfo(
    val order: List<OrderInfo>,
    val count: Int,
    val item: List<ArchiveInfo>? = null,
    val last_watched_locator: LastWatchedLocatorInfo,
    // val episodic_button: { text: String, uri: String }
    val has_next: Boolean,
    val has_prev: Boolean,
) {
    @Serializable
    data class OrderInfo(
        val title: String,
        val value: String,
    )

    @Serializable
    data class LastWatchedLocatorInfo(
        val display_threshold: Int,
        val insert_ranking: Int,
        val text: String,
    )
}