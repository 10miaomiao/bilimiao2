package com.a10miaomiao.bilimiao.widget.comm.ui

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.a10miaomiao.bilimiao.comm.attr
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.comm.AppBarView
import com.a10miaomiao.bilimiao.widget.comm.MenuItemView
import splitties.dimensions.dip
import splitties.views.*
import splitties.views.dsl.core.*

class AppBarHorizontalUi(
    override val ctx: Context,
    val menuItemClick: View.OnClickListener,
    val menuItemLongClick: View.OnLongClickListener,
    val backClick: View.OnClickListener,
    val backLongClick: View.OnLongClickListener,
) : AppBarUi {

    val mNavigationIcon = imageView {
        setBackgroundResource(ctx.attr(android.R.attr.selectableItemBackgroundBorderless))
    }

    val mNavigationIconLayout = frameLayout {
        padding = dip(10)
        bottomPadding = 0
        setOnClickListener(backClick)
        setOnLongClickListener(backLongClick)
        addView(mNavigationIcon, lParams {
            gravity = Gravity.CENTER
            width = dip(24)
            height = dip(24)
        })
    }

    val mTitle = textView {
        gravity = Gravity.CENTER
        setTextColor(config.foregroundAlpha45Color)
    }

    val mTitleLayout = frameLayout {
        padding = dip(10)
        addView(mTitle, lParams {
            width = matchParent
            height = wrapContent
        })
    }

    val mNavigationMemuLayout = verticalLayout {
        gravity = Gravity.CENTER_HORIZONTAL
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
        addView(mNavigationMemuLayout, lParams {
            width = matchParent
            height = matchParent
            weight = 1f
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
            if (prop.title != null) {
                mTitleLayout.visibility = View.VISIBLE
                mTitle.text = prop.title ?: ""
            } else {
                mTitleLayout.visibility = View.GONE
            }

            val menus = prop.menus
            if (menus == null) {
                mNavigationMemuLayout.removeAllViews()
            } else {
                mNavigationMemuLayout.apply {
                    menus.forEachIndexed { index, menu ->
                        var menuItemView: MenuItemView
                        if (index >= childCount) {
                            menuItemView = MenuItemView(ctx)
                            menuItemView.orientation = LinearLayout.HORIZONTAL
                            menuItemView.minimumHeight = dip(40)
                            menuItemView.setOnClickListener(menuItemClick)
                            menuItemView.setBackgroundResource(config.selectableItemBackground)
                            addView(menuItemView, lParams {
                                width = matchParent
                                height = wrapContent
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