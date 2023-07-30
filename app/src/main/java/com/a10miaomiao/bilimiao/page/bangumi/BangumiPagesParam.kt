package com.a10miaomiao.bilimiao.page.bangumi

import android.os.Parcelable
import com.a10miaomiao.bilimiao.comm.entity.bangumi.DimensionXInfo
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BangumiPagesParam(
    val sid: String,
    val title: String,
    val episodes: List<Episode>,
): Parcelable {
    @Parcelize
    data class Episode(
        val aid: String,
        val cid: String,
        val cover: String,
        val ep_id: String,
        val index: String,
        val index_title: String,
        val badge: String,
        val badge_info: EpisodeBadgeInfo,
    ): Parcelable

    @Parcelize
    data class EpisodeBadgeInfo(
        val bg_color: String,
        val bg_color_night: String,
        val text: String,
    ) : Parcelable
}
