package com.a10miaomiao.bilimiao.config

import android.content.Context
import android.view.View
import org.jetbrains.anko.dip

class ViewConfig(val context: Context) {
    val dividerSize = context.dip(10)
    val regionIconSize = context.dip(24)

    val background = 0xfff2f2f2.toInt()

    val blackAlpha45 = 0x71000000
    val black80 = 0xff222222.toInt()

    val white80 = 0xAAFFFFFF.toInt()
}

inline val Context.config get() = ViewConfig(this)
inline val View.config get() = context.config

