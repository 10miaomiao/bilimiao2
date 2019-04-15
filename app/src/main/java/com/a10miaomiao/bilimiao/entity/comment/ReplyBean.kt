package com.a10miaomiao.bilimiao.entity.comment

import android.os.Parcel
import android.os.Parcelable

data class ReplyBean(
        val action: Int,
        val assist: Int,
        val attr: Int,
        val content: Content,
        val count: Int,
        val ctime: Long,
        val dialog: Int,
        val dialog_str: String,
        val fansgrade: Int,
        val floor: Int,
        val like: Int,
        val member: Member,
        val mid: Int,
        val oid: String, //--
        val parent: Int,
        val parent_str: String,
        val rcount: Int,
        val replies: ArrayList<ReplyBean>,
        val root: Int,
        val root_str: String,
        val rpid: Int,
        val rpid_str: String, //--
        val state: Int,
        val type: Int
): Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readParcelable(Content::class.java.classLoader),
            parcel.readInt(),
            parcel.readLong(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readParcelable(Member::class.java.classLoader),
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.createTypedArrayList(CREATOR),
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(action)
        parcel.writeInt(assist)
        parcel.writeInt(attr)
        parcel.writeParcelable(content, flags)
        parcel.writeInt(count)
        parcel.writeLong(ctime)
        parcel.writeInt(dialog)
        parcel.writeString(dialog_str)
        parcel.writeInt(fansgrade)
        parcel.writeInt(floor)
        parcel.writeInt(like)
        parcel.writeParcelable(member, flags)
        parcel.writeInt(mid)
        parcel.writeString(oid)
        parcel.writeInt(parent)
        parcel.writeString(parent_str)
        parcel.writeInt(rcount)
        parcel.writeTypedList(replies)
        parcel.writeInt(root)
        parcel.writeString(root_str)
        parcel.writeInt(rpid)
        parcel.writeString(rpid_str)
        parcel.writeInt(state)
        parcel.writeInt(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ReplyBean> {
        override fun createFromParcel(parcel: Parcel): ReplyBean {
            return ReplyBean(parcel)
        }

        override fun newArray(size: Int): Array<ReplyBean?> {
            return arrayOfNulls(size)
        }
    }

}