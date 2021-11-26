package com.a10miaomiao.bilimiao.widget.comm.ui

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.get
import com.a10miaomiao.bilimiao.comm.attr
import com.a10miaomiao.bilimiao.widget.comm.AppBarView
import com.a10miaomiao.bilimiao.widget.comm.MenuItemView
import splitties.dimensions.dip
import splitties.views.dsl.core.*
import splitties.views.endPadding
import splitties.views.imageDrawable
import splitties.views.padding

class AppBarVerticalUi(
    override val ctx: Context,
    val menuItemClick: View.OnClickListener,
) : AppBarUi {
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

    val mNavigationMemuLayout = horizontalLayout {
        gravity = Gravity.END
        endPadding = dip(10)
    }

    val mNavigationLayout = horizontalLayout {
        addView(mNavigationIconLayout, lParams {
            width = wrapContent
            height = wrapContent
        })
        addView(mNavigationMemuLayout, lParams {
            width = matchParent
            height = matchParent
            weight = 1f
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
            height = dip(44)
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

            val menus = prop.menus
            if (menus == null) {
                mNavigationMemuLayout.removeAllViews()
            } else {
                mNavigationMemuLayout.apply {
                    menus.reversed().forEachIndexed { index, menu ->
                        var menuItemView: MenuItemView
                        if (index >= childCount) {
                            menuItemView = MenuItemView(ctx)
                            menuItemView.orientation = LinearLayout.VERTICAL
                            menuItemView.minimumWidth = dip(60)
                            menuItemView.setOnClickListener(menuItemClick)
                            addView(menuItemView, lParams {
                                width = wrapContent
                                height = matchParent
                            })
                        } else {
                            menuItemView = getChildAt(index) as MenuItemView
                        }
                        menuItemView.prop = menu
                    }
                    if (childCount > menus.size) {
                        removeViews(
                            menus.size,
                            childCount - menus.size
                        )
                    }
                }
            }
        }
    }
}