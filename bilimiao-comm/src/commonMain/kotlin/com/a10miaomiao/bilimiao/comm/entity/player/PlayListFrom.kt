package com.a10miaomiao.bilimiao.comm.entity.player

import kotlinx.serialization.Serializable

@Serializable
sealed class PlayListFrom {
    @Serializable
    class Video(
        val aid: String
    ): PlayListFrom()

    @Serializable
    class Favorite(
        val mediaId: String
    ): PlayListFrom()

    @Serializable
    class Season(
        val seasonId: String
    ): PlayListFrom()

    @Serializable
    class Section(
        val seasonId: String,
        val sectionId: String,
    ): PlayListFrom()

    @Serializable
    class Medialist(
        val bizId: String,
        val bizType: String,
    ): PlayListFrom()

    @Serializable
    class Toview(
        val sortField: Int, // 1全部, 10未看完
        val asc: Boolean = false,
    ): PlayListFrom()
}