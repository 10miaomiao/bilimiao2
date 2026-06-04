package com.a10miaomiao.bilimiao.comm.entity.archive

import kotlinx.serialization.Serializable

/**
 * 稿件关联信息
 */
@Serializable
data class ArchiveRelationInfo(
    val attention: Boolean,
    val favorite: Boolean,
    val season_fav: Boolean,
    val like: Boolean,
    val dislike: Boolean,
    val coin: Int
)