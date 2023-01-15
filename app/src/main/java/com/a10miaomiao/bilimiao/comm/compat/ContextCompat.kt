package com.a10miaomiao.bilimiao.comm.compat

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.appcompat.widget.TintTypedArray
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@SuppressLint("RestrictedApi")
fun Context.obtainStyledAttributesCompat(
    set: AttributeSet? = null,
    @StyleableRes attrs: IntArray,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
): TintTypedArray =
    TintTypedArray.obtainStyledAttributes(this, set, attrs, defStyleAttr, defStyleRes)


@SuppressLint("RestrictedApi")
fun Context.getColorStateListByAttr(@AttrRes attr: Int): ColorStateList =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use { it.getColorStateList(0) }


@OptIn(ExperimentalContracts::class)
@SuppressLint("RestrictedApi")
inline fun <R> TintTypedArray.use(block: (TintTypedArray) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return try {
        block(this)
    } finally {
        recycle()
    }
}