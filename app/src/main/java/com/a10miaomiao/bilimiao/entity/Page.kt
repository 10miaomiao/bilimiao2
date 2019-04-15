package com.a10miaomiao.bilimiao.entity

import android.os.Parcel
import android.os.Parcelable

data class Page(
    val cid: Long,
    val dimension: Dimension,
//    val dm: Dm,
    val dmlink: String,
    val duration: Int,
    val from: String,
    val metas: List<Meta>,
    val page: Int,
    val part: String,
    val vid: String,
    val weblink: String
): Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readParcelable(Dimension::class.java.classLoader),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.createTypedArrayList(Meta),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(cid)
        parcel.writeParcelable(dimension, flags)
        parcel.writeString(dmlink)
        parcel.writeInt(duration)
        parcel.writeString(from)
        parcel.writeTypedList(metas)
        parcel.writeInt(page)
        parcel.writeString(part)
        parcel.writeString(vid)
        parcel.writeString(weblink)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Page> {
        override fun createFromParcel(parcel: Parcel): Page {
            return Page(parcel)
        }

        override fun newArray(size: Int): Array<Page?> {
            return arrayOfNulls(size)
        }
    }

}