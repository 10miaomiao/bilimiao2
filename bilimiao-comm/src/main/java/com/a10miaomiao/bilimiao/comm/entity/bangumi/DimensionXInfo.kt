package com.a10miaomiao.bilimiao.comm.entity.bangumi

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class DimensionXInfo(
    val height: Int,
    val rotate: Int,
    val width: Int
) : Parcelable