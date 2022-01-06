package com.a10miaomiao.bilimiao.comm

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import androidx.core.widget.NestedScrollView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import splitties.views.dsl.core.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun View.wrapInNestedScrollView(
    @IdRes id: Int = View.NO_ID,
    height: Int = wrapContent,
    gravity: Int = FrameLayout.LayoutParams.UNSPECIFIED_GRAVITY,
    initView: NestedScrollView.() -> Unit = {}
): NestedScrollView {
    return view({ NestedScrollView(it) }, id) {
        add(this@wrapInNestedScrollView, lParams(matchParent, height, gravity))
    }.apply(initView)
}

inline fun View.wrapInSwipeRefreshLayout(
    @IdRes id: Int = View.NO_ID,
    height: Int = matchParent,
    initView: SwipeRefreshLayout.() -> Unit = {}
): SwipeRefreshLayout {
    return view({ SwipeRefreshLayout(it) }, id) {
        addView(
            this@wrapInSwipeRefreshLayout,
            ViewGroup.LayoutParams(matchParent, height)
        )
    }.apply(initView)
}

inline fun View.progressBar(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: ProgressBar.() -> Unit = {}
): ProgressBar {
    return view({ ProgressBar(it) }, id).apply(initView)
}

inline fun View.viewPager(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: ViewPager.() -> Unit = {}
): ViewPager {
    return view({ ViewPager(it) }, id).apply(initView)
}

inline fun View.viewPager2(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: ViewPager2.() -> Unit = {}
): ViewPager2 {
    return view({ ViewPager2(it) }, id).apply(initView)
}

inline fun View.tabLayout(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: TabLayout.() -> Unit = {}
): TabLayout {
    return view({ TabLayout(it) }, id).apply(initView)
}


