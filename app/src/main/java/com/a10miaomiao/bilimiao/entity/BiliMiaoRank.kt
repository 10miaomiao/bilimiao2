package com.a10miaomiao.bilimiao.entity

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable

data class BiliMiaoRank (
    var rank_name: String,
    var rank_info: String,
    var rank_pic: String,
    var type: String,
    var url: String,
    var category: List<Category>,
    var filter: List<Filter>
): Parcelable{

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.createTypedArrayList(Category),
            parcel.createTypedArrayList(Filter)) {
    }

    data class Category(
            var id: Int,
            var name: String
    ): Parcelable{
        constructor(parcel: Parcel) : this(
                parcel.readInt(),
                parcel.readString())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeString(name)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Category> {
            override fun createFromParcel(parcel: Parcel): Category {
                return Category(parcel)
            }

            override fun newArray(size: Int): Array<Category?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class Filter(
            var name: String,
            var values: List<FilterItem>
    ): Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                TODO("values")) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Filter> {
            override fun createFromParcel(parcel: Parcel): Filter {
                return Filter(parcel)
            }

            override fun newArray(size: Int): Array<Filter?> {
                return arrayOfNulls(size)
            }
        }

    }


    data class FilterItem(
            var name: String,
            var value: String
    ): Parcelable {

        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeString(value)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<FilterItem> {
            override fun createFromParcel(parcel: Parcel): FilterItem {
                return FilterItem(parcel)
            }

            override fun newArray(size: Int): Array<FilterItem?> {
                return arrayOfNulls(size)
            }
        }

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(rank_name)
        parcel.writeString(rank_info)
        parcel.writeString(rank_pic)
        parcel.writeString(type)
        parcel.writeString(url)
        parcel.writeTypedList(category)
        parcel.writeTypedList(filter)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BiliMiaoRank> {
        override fun createFromParcel(parcel: Parcel): BiliMiaoRank {
            return BiliMiaoRank(parcel)
        }

        override fun newArray(size: Int): Array<BiliMiaoRank?> {
            return arrayOfNulls(size)
        }
    }

}