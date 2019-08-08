package com.a10miaomiao.bilimiao.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BiliMiaoRank (
    var rank_name: String,
    var rank_info: String,
    var rank_pic: String,
    var type: String,
    var url: String,
    var category: List<Category>,
    var filter: List<Filter>
): Parcelable{

    @Parcelize
    data class Category(
            var id: Int,
            var name: String
    ): Parcelable

    @Parcelize
    data class Filter(
            var name: String,
            var values: List<FilterItem>
    ): Parcelable

    @Parcelize
    data class FilterItem(
            var name: String,
            var value: String
    ): Parcelable

}