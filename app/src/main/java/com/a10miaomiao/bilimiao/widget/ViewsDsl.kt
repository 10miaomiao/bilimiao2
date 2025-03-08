package com.a10miaomiao.bilimiao.widget

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import com.a10miaomiao.bilimiao.widget.image.RCImageView
import com.a10miaomiao.bilimiao.widget.layout.LimitedFrameLayout
import com.a10miaomiao.bilimiao.widget.text.BadgeTextView
import splitties.views.dsl.core.*

inline fun View.rcImageView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: RCImageView.() -> Unit = {}
): RCImageView {
    return view({ RCImageView(it) }, id).apply(initView)
}
inline fun View.limitedFrameLayout(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: LimitedFrameLayout.() -> Unit = {}
): LimitedFrameLayout {
    return view({ LimitedFrameLayout(it) }, id).apply(initView)
}

inline fun View.badgeTextView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: BadgeTextView.() -> Unit = {}
): BadgeTextView {
    return view({ BadgeTextView(it) }, id).apply(initView)
}

