package com.a10miaomiao.bilimiao.comm.entity.player

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
sealed class PlayListFrom : Parcelable {
    @Parcelize
    class Video(
        val aid: String
    ): PlayListFrom()

    @Parcelize
    class Favorite(
        val mediaId: String
    ): PlayListFrom()

    @Parcelize
    class Season(
        val seasonId: String
    ): PlayListFrom()

    @Parcelize
    class Section(
        val seasonId: String,
        val sectionId: String,
    ): PlayListFrom()

    @Parcelize
    class Medialist(
        val bizId: String,
        val bizType: String,
    ): PlayListFrom()

    @Parcelize
    class Toview(
        val sortField: Int, // 1全部, 10未看完
        val asc: Boolean = false,
    ): PlayListFrom()
}