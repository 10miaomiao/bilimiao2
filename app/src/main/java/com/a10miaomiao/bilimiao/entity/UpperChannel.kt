package com.a10miaomiao.bilimiao.entity

import android.os.Parcel
import android.os.Parcelable

data class UpperChannel(
        var cid : Int,
        var mid : Int,
        var name : String,
        var intro : String,
        var mtime : Long,
        var count : Int,
        var cover : String,
//        var archives : List<UpperArchives>,
        var isAll: Boolean = false
): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(cid)
        parcel.writeInt(mid)
        parcel.writeString(name)
        parcel.writeString(intro)
        parcel.writeLong(mtime)
        parcel.writeInt(count)
        parcel.writeString(cover)
        parcel.writeByte(if (isAll) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UpperChannel> {
        override fun createFromParcel(parcel: Parcel): UpperChannel {
            return UpperChannel(parcel)
        }

        override fun newArray(size: Int): Array<UpperChannel?> {
            return arrayOfNulls(size)
        }
    }
}