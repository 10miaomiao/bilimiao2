package com.a10miaomiao.bilimiao.entity.bangumi

import android.os.Parcel
import android.os.Parcelable

data class BangumiEpisode(
        val aid: Int,
        val badge: String,
        val badge_type: Int,
        val cid: String,
        val cover: String,
        val duration: Int,
        val ep_id: String,
        val episode_status: Int,
        val from: String,
        val index: String,
        val index_title: String,
        val mid: Int,
        val page: Int,
        val pub_real_time: String,
        val section_id: String,
        val section_type: Int,
        val share_url: String,
        val vid: String
): Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(aid)
        parcel.writeString(badge)
        parcel.writeInt(badge_type)
        parcel.writeString(cid)
        parcel.writeString(cover)
        parcel.writeInt(duration)
        parcel.writeString(ep_id)
        parcel.writeInt(episode_status)
        parcel.writeString(from)
        parcel.writeString(index)
        parcel.writeString(index_title)
        parcel.writeInt(mid)
        parcel.writeInt(page)
        parcel.writeString(pub_real_time)
        parcel.writeString(section_id)
        parcel.writeInt(section_type)
        parcel.writeString(share_url)
        parcel.writeString(vid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BangumiEpisode> {
        override fun createFromParcel(parcel: Parcel): BangumiEpisode {
            return BangumiEpisode(parcel)
        }

        override fun newArray(size: Int): Array<BangumiEpisode?> {
            return arrayOfNulls(size)
        }
    }

}