package com.a10miaomiao.bilimiao.config

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.fragment.app.Fragment
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.attr
import splitties.dimensions.dip
import splitties.views.dsl.core.matchParent

class ViewConfig(val context: Context) {
    val containerWidth = context.dip(900)
    val pagePadding get() = context.dip(10)
    val bottomSheetTitleHeight = context.dip(30)

    val dividerSize get() = context.dip(8)
    val regionIconSize get() = context.dip(24)

    val blackAlpha45 get() = context.resources.getColor(R.color.black_alpha_45)
    val black80 get() = context.resources.getColor(R.color.black_80)

    val white80 get() = 0xAAFFFFFF.toInt()

    val themeColorResource get() = context.attr(android.R.attr.colorPrimary)
    val themeColor get() = context.resources.getColor(themeColorResource)

    val windowBackgroundResource get() = context.attr(R.attr.defaultBackgroundColor)
    val windowBackgroundColor get() = context.resources.getColor(windowBackgroundResource)

    val blockBackgroundResource get() = context.attr(R.attr.blockBackground)
    val blockBackgroundColor get() = context.resources.getColor(blockBackgroundResource)

    val foregroundColorResource get() = context.attr(R.attr.foregroundColor)
    val foregroundColor get() = context.resources.getColor(foregroundColorResource)
    val foregroundAlpha45Color get() = (foregroundColor and 0x00FFFFFF) or 0x71000000


    val lineColorResource get() = context.attr(R.attr.lineColor)
    val lineColor get() = context.resources.getColor(lineColorResource)

    val selectableItemBackground get() = context.attr(android.R.attr.selectableItemBackground)
    val selectableItemBackgroundBorderless get() = context.attr(android.R.attr.selectableItemBackgroundBorderless)
}

inline val Context.config get() = ViewConfig(this)
inline val Fragment.config get() = requireContext().config
inline val View.config get() = context.config

