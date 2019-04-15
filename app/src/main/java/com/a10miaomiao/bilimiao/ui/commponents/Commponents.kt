package com.a10miaomiao.bilimiao.ui.commponents

import android.content.Context
import android.view.View
import android.view.ViewManager
import android.widget.LinearLayout
import com.a10miaomiao.bilimiao.ui.widget.MySpannableTextView
import org.jetbrains.anko.AnkoViewDslMarker
import org.jetbrains.anko._LinearLayout
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.internals.AnkoInternals

inline fun ViewManager.headerView(theme: Int = 0): HeaderView = headerView(theme) {}
inline fun ViewManager.headerView(theme: Int = 0, init: (@AnkoViewDslMarker HeaderView).() -> Unit): HeaderView {
    return ankoView({ ctx: Context -> HeaderView(ctx) }, theme, init)
}

inline fun ViewManager.datePickerView(theme: Int = 0): DatePickerView = datePickerView(theme) {}
inline fun ViewManager.datePickerView(theme: Int = 0, init: (@AnkoViewDslMarker DatePickerView).() -> Unit): DatePickerView {
    return ankoView({ ctx: Context -> DatePickerView(ctx) }, theme, init)
}

inline fun ViewManager.monthPickerView(theme: Int = 0): MonthPickerView = monthPickerView(theme) {}
inline fun ViewManager.monthPickerView(theme: Int = 0, init: (@AnkoViewDslMarker MonthPickerView).() -> Unit): MonthPickerView {
    return ankoView({ ctx: Context -> MonthPickerView(ctx) }, theme, init)
}

inline fun ViewManager.rcLayout(theme: Int = 0): RCLayout = rcLayout(theme) {}
inline fun ViewManager.rcLayout(theme: Int = 0, init: (@AnkoViewDslMarker RCLayout).() -> Unit): RCLayout {
    return ankoView({ ctx: Context -> RCLayout(ctx) }, theme, init)
}

inline fun ViewManager.rcImageView(theme: Int = 0): RCImageView = rcImageView(theme) {}
inline fun ViewManager.rcImageView(theme: Int = 0, init: (@AnkoViewDslMarker RCImageView).() -> Unit): RCImageView {
    return ankoView({ ctx: Context -> RCImageView(ctx) }, theme, init)
}


inline fun ViewManager.loadMoreView(theme: Int = 0): LoadMoreView = loadMoreView(theme) {}
inline fun ViewManager.loadMoreView(theme: Int = 0, init: (@AnkoViewDslMarker LoadMoreView).() -> Unit): LoadMoreView {
    return ankoView({ ctx: Context -> LoadMoreView(ctx) }, theme, init)
}

inline fun ViewManager.dropMenuView(theme: Int = 0): DropMenuView = dropMenuView(theme) {}
inline fun ViewManager.dropMenuView(theme: Int = 0, init: (@AnkoViewDslMarker DropMenuView).() -> Unit): DropMenuView {
    return ankoView({ ctx: Context -> DropMenuView(ctx) }, theme, init)
}

inline fun ViewManager.mySpannableTextView(theme: Int = 0): MySpannableTextView = mySpannableTextView(theme) {}
inline fun ViewManager.mySpannableTextView(theme: Int = 0, init: (@AnkoViewDslMarker MySpannableTextView).() -> Unit): MySpannableTextView {
    return ankoView({ ctx: Context -> MySpannableTextView(ctx) }, theme, init)
}

inline fun ViewManager.commentItemView(theme: Int = 0): CommentItemView = commentItemView(theme) {}
inline fun ViewManager.commentItemView(theme: Int = 0, init: (@AnkoViewDslMarker CommentItemView).() -> Unit): CommentItemView {
    return ankoView({ ctx: Context -> CommentItemView(ctx) }, theme, init)
}

