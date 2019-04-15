package com.a10miaomiao.bilimiao.entity.comment

import android.os.Parcel
import android.os.Parcelable

data class Content(
        val device: String,
//        val members: List<Any>,
        val message: String,
        val plat: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(device)
        parcel.writeString(message)
        parcel.writeInt(plat)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Content> {
        override fun createFromParcel(parcel: Parcel): Content {
            return Content(parcel)
        }

        override fun newArray(size: Int): Array<Content?> {
            return arrayOfNulls(size)
        }
    }

}
