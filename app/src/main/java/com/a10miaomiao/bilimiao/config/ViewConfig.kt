package com.a10miaomiao.bilimiao.config

import android.content.Context
import android.graphics.Color
import android.view.View
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.utils.attr
import org.jetbrains.anko.dip

class ViewConfig(val context: Context) {
    val dividerSize get() = context.dip(10)
    val regionIconSize get() = context.dip(24)

    val blackAlpha45 get() = context.resources.getColor(R.color.black_alpha_45)
    val black80 get() = context.resources.getColor(R.color.black_80)

    val white80 get() = 0xAAFFFFFF.toInt()

    val themeColorResource get() = context.attr(android.R.attr.colorPrimary)
    val themeColor get() = context.resources.getColor(themeColorResource)

    val windowBackgroundResource get() = context.attr(android.R.attr.windowBackground)
    val windowBackgroundColor get() = context.resources.getColor(windowBackgroundResource)

    val blockBackgroundResource get() = context.attr(R.attr.blockBackground)
    val blockBackgroundColor get() = context.resources.getColor(blockBackgroundResource)

    val foregroundColorResource get() = context.attr(R.attr.foregroundColor)
    val foregroundColor get() = context.resources.getColor(foregroundColorResource)
    val foregroundAlpha45Color get() = (foregroundColor and 0x00FFFFFF) or 0x71000000


    val lineColorResource get() = context.attr(R.attr.lineColor)
    val lineColor get() = context.resources.getColor(lineColorResource)

}

inline val Context.config get() = ViewConfig(this)
inline val View.config get() = context.config

