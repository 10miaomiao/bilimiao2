package com.a10miaomiao.bilimiao.entity

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

class Home {

    data class RegionData(
            var data: List<Region>
    )

    /**
     * 分区信息
     */
    @Parcelize
    data class Region(
            var tid: Int,
            var reid: Int,
            var icon: Int?,
            var logo: String?,
            var name: String,
            var uri: String,
            var type: Int,
            var children: List<RegionChildren>
    ) : Parcelable

    /**
     * 子分区信息
     */
    @Parcelize
    data class RegionChildren(
            var tid: Int,
            var reid: Int,
            var name: String,
            var type: Int
    ) : Parcelable
}
