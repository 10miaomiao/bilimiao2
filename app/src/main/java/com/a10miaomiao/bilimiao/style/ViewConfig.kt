package com.a10miaomiao.bilimiao.config

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.attr
import splitties.dimensions.dip
import splitties.views.dsl.core.matchParent

class ViewConfig(val context: Context) {
    val pagePadding = context.dip(10)
    val bottomSheetTitleHeight = context.dip(48)

    val smallPadding = context.dip(5)
    val largePadding = context.dip(20)

    val dividerSize = context.dip(8)

    val themeName = context.resources.getString(context.attr(R.attr.themeName))


    val themeColorResource = context.attr(android.R.attr.colorPrimary)
    val themeColor = getColor(themeColorResource)

    val windowBackgroundResource = context.attr(R.attr.defaultBackgroundColor)
    val windowBackgroundColor = getColor(windowBackgroundResource)

    //    val blockBackgroundResource = context.attr(com.google.android.material.R.attr.colorSurface)
    val blockBackgroundResource = context.attr(R.attr.blockBackground)
    val blockBackgroundColor = getColor(blockBackgroundResource)
    val blockBackgroundAlpha45Color = (blockBackgroundColor and 0x00FFFFFF) or 0x71000000

    val foregroundColorResource = context.attr(R.attr.foregroundColor)
    val foregroundColor = getColor(foregroundColorResource)
    val foregroundAlpha45Color = (foregroundColor and 0x00FFFFFF) or 0x71000000

    val foregroundAlpha80Color = (foregroundColor and 0x00FFFFFF) or 0xCC000000.toInt()

    private val isLightThemeResource = context.attr(R.attr.isLightTheme)
    val isLightTheme = context.resources.getBoolean(isLightThemeResource)

    val lineColorResource = context.attr(R.attr.lineColor)
    val lineColor = getColor(lineColorResource)
    val shadowColorResource = context.attr(R.attr.shadowColor)
    val shadowColor = getColor(shadowColorResource)

    val selectableItemBackground = context.attr(android.R.attr.selectableItemBackground)
    val selectableItemBackgroundBorderless =
        context.attr(android.R.attr.selectableItemBackgroundBorderless)

    val appBarHeight = context.dip(70)
    val appBarTitleHeight = context.dip(20)
    val appBarMenuHeight = context.dip(50)
    val appBarMenuWidth = context.dip(120)

    internal fun getColor(resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }
}

@SuppressLint("StaticFieldLeak")
private object BackendViewConfig {
    var config: ViewConfig? = null
    var configContext: Context? = null
}

fun Context.resetViewConfig() {
    if (this == BackendViewConfig.configContext) {
        BackendViewConfig.config = null
        BackendViewConfig.configContext = null
    }
}
@get:Synchronized
val Context.config: ViewConfig
    get() {
        return BackendViewConfig.config?.takeIf {
            this === BackendViewConfig.configContext
        } ?: ViewConfig(this).also {
            BackendViewConfig.config = it
            BackendViewConfig.configContext = this
        }
    }
inline val Fragment.config get() = requireContext().config
inline val View.config get() = context.config
