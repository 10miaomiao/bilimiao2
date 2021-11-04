package com.a10miaomiao.bilimiao.widget.comm.ui

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.widget.comm.AppBarView
import splitties.dimensions.dip
import splitties.views.dsl.core.*
import splitties.views.imageDrawable

class AppBarVerticalUi(override val ctx: Context) : AppBarUi {
    val mTitle = textView {
        gravity = Gravity.CENTER
        textSize = 12f
    }

    override val root = frameLayout {
        layoutParams = ViewGroup.LayoutParams(matchParent, dip(50))
        addView(mTitle)
    }

    override fun setProp(prop: AppBarView.PropInfo?) {
        if (prop != null) {
            if (prop.navigationIcon != null) {
//                mNavigationIconLayout.visibility = View.VISIBLE
//                mNavigationIcon.imageDrawable = prop.navigationIcon
            }
            if (prop.onNavigationClick != null) {
//                mNavigationIconLayout.setOnClickListener(prop.onNavigationClick)
            }
            if (prop.title != null) {
//                mTitleLayout.visibility = View.VISIBLE
                mTitle.text = prop.title ?: ""
            }

        }
    }
}