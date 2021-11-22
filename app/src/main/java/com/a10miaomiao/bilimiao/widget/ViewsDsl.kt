package com.a10miaomiao.bilimiao.widget

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import com.a10miaomiao.bilimiao.widget.expandabletext.ExpandableTextView
import com.a10miaomiao.bilimiao.widget.image.RCImageView
import com.a10miaomiao.bilimiao.widget.picker.DatePickerView
import com.a10miaomiao.bilimiao.widget.picker.MonthPickerView
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

inline fun View.expandableTextView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: ExpandableTextView.() -> Unit = {}
): ExpandableTextView {
    return view({ ExpandableTextView(it) }, id).apply(initView)
}

inline fun View.datePickerView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: DatePickerView.() -> Unit = {}
): DatePickerView {
    return view({ DatePickerView(it) }, id).apply(initView)
}

inline fun View.monthPickerView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: MonthPickerView.() -> Unit = {}
): MonthPickerView {
    return view({ MonthPickerView(it) }, id).apply(initView)
}