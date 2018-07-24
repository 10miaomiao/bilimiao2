package com.a10miaomiao.bilimiao.config

import android.content.Context
import android.view.View
import com.a10miaomiao.miaoandriod.MiaoAnkoContext
import org.jetbrains.anko.dip

class ViewConfig(val context: Context) {
    val dividerSize = context.dip(10)
    val regionIconSize = context.dip(24)
}

inline val Context.config get() = ViewConfig(this)
inline val View.config get() = context.config

