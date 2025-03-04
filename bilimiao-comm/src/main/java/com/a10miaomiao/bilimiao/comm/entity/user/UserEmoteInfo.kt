package com.a10miaomiao.bilimiao.comm.entity.user

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class UserEmoteInfo (
    val id: Int,
//    val package_id: Int,
    val text: String,
    val url: String,
//    val mtime: Long,
//    val type: Int,
//    val attr: Int,
): Parcelable
