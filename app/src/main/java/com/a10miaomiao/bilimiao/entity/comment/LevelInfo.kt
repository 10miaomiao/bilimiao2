package com.a10miaomiao.bilimiao.entity.comment

import android.os.Parcel
import android.os.Parcelable

data class LevelInfo(
        val current_exp: Int,
        val current_level: Int,
        val current_min: Int,
        val next_exp: Int
): Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(current_exp)
        parcel.writeInt(current_level)
        parcel.writeInt(current_min)
        parcel.writeInt(next_exp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LevelInfo> {
        override fun createFromParcel(parcel: Parcel): LevelInfo {
            return LevelInfo(parcel)
        }

        override fun newArray(size: Int): Array<LevelInfo?> {
            return arrayOfNulls(size)
        }
    }
}