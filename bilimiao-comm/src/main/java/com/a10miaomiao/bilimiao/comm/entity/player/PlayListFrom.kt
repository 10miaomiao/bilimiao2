package com.a10miaomiao.bilimiao.comm.entity.player

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
sealed class PlayListFrom : Parcelable {
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
}