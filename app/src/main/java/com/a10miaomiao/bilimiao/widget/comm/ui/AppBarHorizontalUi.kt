package com.a10miaomiao.bilimiao.widget.comm.ui

import android.content.Context
import android.view.View
import com.a10miaomiao.bilimiao.comm.attr
import com.a10miaomiao.bilimiao.widget.comm.AppBarView
import splitties.dimensions.dip
import splitties.views.dsl.core.*
import splitties.views.imageDrawable
import splitties.views.padding
import splitties.views.topPadding

class AppBarHorizontalUi(override val ctx: Context) : AppBarUi {

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

    val mTitle = textView {

    }

    val mTitleLayout = frameLayout {
        padding = dip(10)
        topPadding = 0
        addView(mTitle, lParams {
            width = matchParent
            height = wrapContent
        })
    }

    override val root = verticalLayout {
        addView(mNavigationIconLayout, lParams {
            width = matchParent
            height = wrapContent
        })
        addView(mTitleLayout, lParams {
            width = matchParent
            height = wrapContent
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
            if (prop.title != null) {
                mTitleLayout.visibility = View.VISIBLE
                mTitle.text = prop.title ?: ""
            } else {
                mTitleLayout.visibility = View.GONE
            }
        }
    }
}