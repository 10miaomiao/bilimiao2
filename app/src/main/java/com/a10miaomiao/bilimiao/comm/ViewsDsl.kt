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
import com.a10miaomiao.bilimiao.widget.shadow.ShadowLayout
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import splitties.views.dsl.core.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun View.wrapInMaterialCardView(
    @IdRes id: Int = View.NO_ID,
    height: Int = matchParent,
    initView: MaterialCardView.() -> Unit = {}
): MaterialCardView {
    return view({ MaterialCardView(it) }, id) {
        addView(
            this@wrapInMaterialCardView,
            ViewGroup.LayoutParams(matchParent, height)
        )
    }.apply(initView)
}


inline fun View.flexboxLayout(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: FlexboxLayout.() -> Unit = {}
): FlexboxLayout {
    return view({ FlexboxLayout(it) }, id).apply(initView)
}


