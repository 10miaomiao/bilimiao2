package com.a10miaomiao.bilimiao.widget.comm.ui

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.comm.attr
import com.a10miaomiao.bilimiao.widget.comm.AppBarView
import splitties.dimensions.dip
import splitties.views.dsl.core.*
import splitties.views.imageDrawable
import splitties.views.padding

class AppBarVerticalUi(override val ctx: Context) : AppBarUi {
    val mTitle = textView {
        gravity = Gravity.CENTER
        textSize = 12f
    }

    val mNavigationIcon = imageView {
        setBackgroundResource(ctx.attr(android.R.attr.selectableItemBackgroundBorderless))
    }

    val mNavigationIconLayout = frameLayout {
        padding = dip(10)
        addView(mNavigationIcon, lParams {
            width = dip(24)
            height = dip(24)
        })
    }

    val mNavigationLayout = horizontalLayout {
        addView(mNavigationIconLayout, lParams {
            width = wrapContent
            height = wrapContent
        })
    }

    override val root = frameLayout {
        layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)
        addView(mTitle, lParams {
            width = matchParent
            height = wrapContent
        })
        addView(mNavigationLayout, lParams {
            topMargin = dip(15)
            width = matchParent
            height = matchParent
        })
    }

    override fun setProp(prop: AppBarView.PropInfo?) {
        if (prop != null) {
            if (prop.navigationIcon != null) {
                mNavigationIconLayout.visibility = View.VISIBLE
                mNavigationIcon.imageDrawable = prop.navigationIcon
            } else {
                mNavigationIconLayout.visibility = View.GONE
            }
            if (prop.onNavigationClick != null) {
                mNavigationIcon.setOnClickListener(prop.onNavigationClick)
            }
            mTitle.text = (prop.title ?: "").replace("\n", " ")
        }
    }
}