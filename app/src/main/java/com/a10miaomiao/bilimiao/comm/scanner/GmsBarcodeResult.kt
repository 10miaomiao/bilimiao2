package com.a10miaomiao.bilimiao.comm.scanner

import android.os.Parcelable.Creator
import org.microg.safeparcel.AutoSafeParcelable
import org.microg.safeparcel.SafeParcelable

class GmsBarcodeResult : AutoSafeParcelable() {
    @SafeParcelable.Field(1)
    var format: Int = 0

    @SafeParcelable.Field(2)
    var displayValue: String = ""

//    @SafeParcelable.Field(3)
//    var rawValue: String = ""
//
//    @SafeParcelable.Field(4)
//    var rawBytes: ByteArray? = null
//
//    @SafeParcelable.Field(6)
//    var valueType: Int = 0

    companion object {
        @JvmField
        val CREATOR: Creator<GmsBarcodeResult> = AutoCreator<GmsBarcodeResult>(
            GmsBarcodeResult::class.java
        )
    }
}