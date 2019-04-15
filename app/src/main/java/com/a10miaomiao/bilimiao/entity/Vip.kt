package com.a10miaomiao.bilimiao.entity

import android.os.Parcel
import android.os.Parcelable

data class Vip(
    val accessStatus: Int,
    val dueRemark: String,
    val vipDueDate: Double,
    val vipStatus: Int,
    val vipStatusWarn: String,
    val vipType: Int
): Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(accessStatus)
        parcel.writeString(dueRemark)
        parcel.writeDouble(vipDueDate)
        parcel.writeInt(vipStatus)
        parcel.writeString(vipStatusWarn)
        parcel.writeInt(vipType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Vip> {
        override fun createFromParcel(parcel: Parcel): Vip {
            return Vip(parcel)
        }

        override fun newArray(size: Int): Array<Vip?> {
            return arrayOfNulls(size)
        }
    }

}