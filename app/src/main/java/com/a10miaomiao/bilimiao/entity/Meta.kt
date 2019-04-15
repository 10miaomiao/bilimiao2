package com.a10miaomiao.bilimiao.entity

import android.os.Parcel
import android.os.Parcelable

data class Meta(
    val format: String,
    val quality: Int,
    val size: Int
): Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(format)
        parcel.writeInt(quality)
        parcel.writeInt(size)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Meta> {
        override fun createFromParcel(parcel: Parcel): Meta {
            return Meta(parcel)
        }

        override fun newArray(size: Int): Array<Meta?> {
            return arrayOfNulls(size)
        }
    }

}