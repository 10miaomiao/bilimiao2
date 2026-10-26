package com.a10miaomiao.bilimiao.comm

import android.content.Context
import android.util.TypedValue

fun Context.attr(resid: Int): Int {
    val typedValue = TypedValue()
    this.theme.resolveAttribute(resid, typedValue, true)
    return typedValue.resourceId
}
