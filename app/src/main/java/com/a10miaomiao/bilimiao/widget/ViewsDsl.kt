package com.a10miaomiao.bilimiao.widget

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import com.a10miaomiao.bilimiao.widget.image.RCImageView
import com.google.android.material.tabs.TabLayout
import splitties.views.dsl.core.NO_THEME
import splitties.views.dsl.core.view

inline fun View.rcImageView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: RCImageView.() -> Unit = {}
): RCImageView {
    return view({ RCImageView(it) }, id).apply(initView)
}