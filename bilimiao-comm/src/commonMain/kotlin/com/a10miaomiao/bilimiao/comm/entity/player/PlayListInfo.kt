package com.a10miaomiao.bilimiao.comm.entity.player

import kotlinx.serialization.Serializable

@Serializable
data class PlayListInfo(
    val name: String,
    val from: PlayListFrom,
    val items: List<PlayListItemInfo>,
)