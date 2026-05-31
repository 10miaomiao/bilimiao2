package com.a10miaomiao.bilimiao.comm.utils

import android.content.Context
import android.view.View

fun Context.dip(dp: Int): Int =
    (dp * resources.displayMetrics.density + 0.5f).toInt()

fun Context.dip(dp: Float): Int =
    (dp * resources.displayMetrics.density + 0.5f).toInt()

fun Context.dipFloat(dp: Int): Float =
    dp * resources.displayMetrics.density

fun Context.dipFloat(dp: Float): Float =
    dp * resources.displayMetrics.density

fun View.dip(dp: Int): Int = context.dip(dp)

fun View.dip(dp: Float): Int = context.dip(dp)

fun View.dipFloat(dp: Int): Float = context.dipFloat(dp)

fun View.dipFloat(dp: Float): Float = context.dipFloat(dp)
