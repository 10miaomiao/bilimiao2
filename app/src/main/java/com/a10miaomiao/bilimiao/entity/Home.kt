package com.a10miaomiao.bilimiao.entity

import android.os.Parcel
import android.os.Parcelable

class Home {
    data class RegionData(
            var data: List<Region>
    )

    /**
     * 分区信息
     */
    data class Region(
            var tid: Int,
            var reid: Int,
            var icon: Int,
            var name: String,
            var uri: String,
            var type: Int,
            var children: List<RegionChildren>
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readInt(),
                parcel.readInt(),
                parcel.readInt(),
                parcel.readString(),
                parcel.readString(),
                parcel.readInt(),
                parcel.createTypedArrayList(RegionChildren)) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(tid)
            parcel.writeInt(reid)
            parcel.writeInt(icon)
            parcel.writeString(name)
            parcel.writeString(uri)
            parcel.writeInt(type)
            parcel.writeTypedList(children)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Region> {
            override fun createFromParcel(parcel: Parcel): Region {
                return Region(parcel)
            }

            override fun newArray(size: Int): Array<Region?> {
                return arrayOfNulls(size)
            }
        }
    }

    /**
     * 子分区信息
     */
    data class RegionChildren(
            var tid: Int,
            var reid: Int,
            var name: String,
            var type: Int
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readInt(),
                parcel.readInt(),
                parcel.readString(),
                parcel.readInt()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(tid)
            parcel.writeInt(reid)
            parcel.writeString(name)
            parcel.writeInt(type)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<RegionChildren> {
            override fun createFromParcel(parcel: Parcel): RegionChildren {
                return RegionChildren(parcel)
            }

            override fun newArray(size: Int): Array<RegionChildren?> {
                return arrayOfNulls(size)
            }
        }
    }
}
