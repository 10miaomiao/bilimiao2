package com.a10miaomiao.bilimiao.entity.comment

import android.os.Parcel
import android.os.Parcelable
import com.a10miaomiao.bilimiao.entity.OfficialVerify
import com.a10miaomiao.bilimiao.entity.Vip

data class Member(
//        val DisplayRank: String,
        val avatar: String,
//        val fans_detail: Any,
        val following: Int,
        val level_info: LevelInfo,
        val mid: String,
//        val nameplate: Nameplate,
//        val official_verify: OfficialVerify,
//        val pendant: Pendant,
        val rank: String,
        val sex: String,
        val sign: String,
        val uname: String,
        val vip: Vip
): Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt(),
            parcel.readParcelable(LevelInfo::class.java.classLoader),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(Vip::class.java.classLoader)) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(avatar)
        parcel.writeInt(following)
        parcel.writeParcelable(level_info, flags)
        parcel.writeString(mid)
        parcel.writeString(rank)
        parcel.writeString(sex)
        parcel.writeString(sign)
        parcel.writeString(uname)
        parcel.writeParcelable(vip, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Member> {
        override fun createFromParcel(parcel: Parcel): Member {
            return Member(parcel)
        }

        override fun newArray(size: Int): Array<Member?> {
            return arrayOfNulls(size)
        }
    }

}