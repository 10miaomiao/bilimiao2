package com.a10miaomiao.bilimiao.widget

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import com.a10miaomiao.bilimiao.widget.expandabletext.ExpandableTextView
import com.a10miaomiao.bilimiao.widget.gridimage.NineGridImageView
import com.a10miaomiao.bilimiao.widget.image.RCImageView
import com.a10miaomiao.bilimiao.widget.layout.LimitedFrameLayout
import com.a10miaomiao.bilimiao.widget.picker.DatePickerView
import com.a10miaomiao.bilimiao.widget.picker.MonthPickerView
import splitties.views.dsl.core.*

inline fun View.rcImageView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: RCImageView.() -> Unit = {}
): RCImageView {
    return view({ RCImageView(it) }, id).apply(initView)
}

inline fun Ui.expandableTextView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: ExpandableTextView.() -> Unit = {}
): ExpandableTextView {
    return view({ ExpandableTextView(it) }, id).apply(initView)
}

inline fun View.expandableTextView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: ExpandableTextView.() -> Unit = {}
): ExpandableTextView {
    return view({ ExpandableTextView(it) }, id).apply(initView)
}

inline fun View.limitedFrameLayout(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: LimitedFrameLayout.() -> Unit = {}
): LimitedFrameLayout {
    return view({ LimitedFrameLayout(it) }, id).apply(initView)
}

inline fun View.wrapInLimitedFrameLayout(
    maxWidth: Int = 0,
    maxHeight: Int = 0,
    @IdRes id: Int = View.NO_ID,
    initView: LimitedFrameLayout.() -> Unit = {}
): LimitedFrameLayout {
    return view({ LimitedFrameLayout(it) }, id).apply {
        this.maxWidth = maxWidth
        this.maxHeight = maxHeight
        initView()
        addView(this@wrapInLimitedFrameLayout, lParams(matchParent, matchParent))
    }
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

inline fun View.nineGridImageView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: NineGridImageView.() -> Unit = {}
): NineGridImageView {
    return view({ NineGridImageView(it) }, id).apply(initView)
}
