package com.a10miaomiao.bilimiao.entity

import android.os.Parcel
import android.os.Parcelable

data class Dimension(
    val height: Int,
    val rotate: Int,
    val width: Int
): Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(height)
        parcel.writeInt(rotate)
        parcel.writeInt(width)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Dimension> {
        override fun createFromParcel(parcel: Parcel): Dimension {
            return Dimension(parcel)
        }

        override fun newArray(size: Int): Array<Dimension?> {
            return arrayOfNulls(size)
        }
    }
}