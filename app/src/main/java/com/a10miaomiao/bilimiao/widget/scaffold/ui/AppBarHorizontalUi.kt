package com.a10miaomiao.bilimiao.widget.scaffold.ui

import android.animation.Animator
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.a10miaomiao.bilimiao.comm.attr
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.scaffold.AppBarView
import com.a10miaomiao.bilimiao.widget.scaffold.MenuItemView
import splitties.dimensions.dip
import splitties.views.*
import splitties.views.dsl.core.*

class AppBarHorizontalUi(
    override val ctx: Context,
    val menuItemClick: View.OnClickListener,
    val menuItemLongClick: View.OnLongClickListener,
    val backClick: View.OnClickListener,
    val backLongClick: View.OnLongClickListener,
    val pointerClick: View.OnClickListener,
    val pointerLongClick: View.OnLongClickListener,
    val exchangeClick: View.OnClickListener,
    val exchangeLongClick: View.OnLongClickListener,
) : AppBarUi {

    val mNavigationIcon = imageView {
        setBackgroundResource(ctx.attr(android.R.attr.selectableItemBackgroundBorderless))
    }
    val mNavigationPointerIcon = imageView {
        setBackgroundResource(ctx.attr(android.R.attr.selectableItemBackgroundBorderless))
    }
    val mNavigationExchangeIcon = imageView {
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
    val mNavigationPointerIconLayout = frameLayout {
        padding = dip(10)
        bottomPadding = 0
        setOnClickListener(pointerClick)
        setOnLongClickListener(pointerLongClick)
        addView(mNavigationPointerIcon, lParams {
            gravity = Gravity.CENTER
            width = dip(24)
            height = dip(24)
        })
    }
    val mNavigationExchangeIconLayout = frameLayout {
        padding = dip(10)
        bottomPadding = 0
        setOnClickListener(exchangeClick)
        setOnLongClickListener(exchangeLongClick)
        addView(mNavigationExchangeIcon, lParams {
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

        val layoutTransition = LayoutTransition()
        //View出現的動畫
        layoutTransition.setAnimator(LayoutTransition.APPEARING, getInAnim())
        //元素在容器中消失時需要動畫顯示
        layoutTransition.setAnimator(LayoutTransition.DISAPPEARING, getOutAnim())
        setLayoutTransition(layoutTransition)
    }

    val mNavigationLayout = verticalLayout {
        val layoutTransition = LayoutTransition()
        //View出現的動畫
        layoutTransition.setAnimator(LayoutTransition.APPEARING, getInAnim())
        //元素在容器中消失時需要動畫顯示
        layoutTransition.setAnimator(LayoutTransition.DISAPPEARING, getOutAnim())
        setLayoutTransition(layoutTransition)

        addView(mNavigationIconLayout, lParams {
            width = matchParent
            height = wrapContent
        })
        addView(mNavigationPointerIconLayout, lParams {
            width = matchParent
            height = wrapContent
        })
        addView(mNavigationExchangeIconLayout, lParams {
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

    private val lineView = textView {
        backgroundColor = ctx.config.colorSurfaceVariant
    }

    override val root = frameLayout {
        addView(mNavigationLayout.wrapInScrollView {
            scrollBarSize = 0
        }, lParams {
            width = config.appBarMenuWidth
            height = matchParent
            gravity = Gravity.RIGHT
        })
        addView(lineView, lParams(dip(1), matchParent) {
            gravity = Gravity.RIGHT
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
            if (prop.navigationPointerIcon != null) {
                mNavigationPointerIcon.imageDrawable = prop.navigationPointerIcon
                mNavigationPointerIconLayout.visibility = View.VISIBLE
                mNavigationPointerIcon.rotation = if(prop.pointerIconOrientation) 0F else 180F
            } else {
                mNavigationPointerIconLayout.visibility = View.GONE
            }
            if(prop.navigationExchangeIcon != null){
                mNavigationExchangeIcon.imageDrawable = prop.navigationExchangeIcon
                mNavigationExchangeIconLayout.visibility = View.VISIBLE
            } else {
                mNavigationExchangeIconLayout.visibility = View.VISIBLE
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

    override fun updateTheme() {
        lineView.backgroundColor = ctx.config.colorSurfaceVariant
    }

    private fun View.getInAnim(): Animator {
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", 0f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY", 0f, 1f)
        val trAlpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1f)
        return ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY, trAlpha)
    }

    private fun View.getOutAnim(): Animator {
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 0f)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 0f)
        val trAlpha = PropertyValuesHolder.ofFloat("alpha", 1f, 0f)
        return ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY, trAlpha)
    }
}