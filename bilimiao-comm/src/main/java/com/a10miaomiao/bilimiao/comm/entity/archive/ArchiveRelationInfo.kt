package com.a10miaomiao.bilimiao.comm.entity.archive

/**
 * 稿件关联信息
 */
data class ArchiveRelationInfo(
    val attention: Boolean,
    val favorite: Boolean,
    val season_fav: Boolean,
    val like: Boolean,
    val dislike: Boolean,
    val coin: Int
)