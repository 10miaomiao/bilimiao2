package com.a10miaomiao.bilimiao.config

import android.content.Context
import android.view.View
import com.a10miaomiao.bilimiao.utils.attr
import org.jetbrains.anko.dip

class ViewConfig(val context: Context) {
    val dividerSize get() = context.dip(10)
    val regionIconSize get() = context.dip(24)

    val background get() = 0xfff2f2f2.toInt()

    val blackAlpha45 get() = 0x71000000
    val black80 get() = 0xff222222.toInt()

    val white80 get() = 0xAAFFFFFF.toInt()

    val themeColorResource get() = context.attr(android.R.attr.colorPrimary)
    val themeColor get() = context.resources.getColor(themeColorResource)
}

inline val Context.config get() = ViewConfig(this)
inline val View.config get() = context.config

