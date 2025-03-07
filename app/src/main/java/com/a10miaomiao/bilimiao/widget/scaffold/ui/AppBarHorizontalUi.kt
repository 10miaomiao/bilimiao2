package com.a10miaomiao.bilimiao.widget.scaffold.ui

import android.animation.Animator
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.a10miaomiao.bilimiao.comm.attr
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.scaffold.AppBarView
import com.a10miaomiao.bilimiao.widget.scaffold.MenuCheckableItemView
import com.a10miaomiao.bilimiao.widget.scaffold.MenuItemView
import splitties.dimensions.dip
import splitties.views.*
import splitties.views.dsl.core.*

class AppBarHorizontalUi(
    override val ctx: Context,
    val appBarView: AppBarView,
    val menuItemClick: View.OnClickListener,
    val menuItemLongClick: View.OnLongClickListener,
    val navigationClick: View.OnClickListener,
    val navigationLongClick: View.OnLongClickListener,
    val enableSubContent: Boolean,
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
        setOnClickListener(navigationClick)
        setOnLongClickListener(navigationLongClick)
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
        if (enableSubContent) {
            addView(mNavigationPointerIconLayout, lParams {
                width = matchParent
                height = wrapContent
            })
            addView(mNavigationExchangeIconLayout, lParams {
                width = matchParent
                height = wrapContent
            })
        }
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
//        backgroundColor = ctx.config.colorSurfaceVariant
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
            if (prop.navigationButtonIcon != null) {
                mNavigationIconLayout.visibility = View.VISIBLE
                mNavigationIcon.imageDrawable = prop.navigationButtonIcon
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
            } else if (prop.isNavigationMenu) {
                // 导航栏
                mNavigationMemuLayout.apply {
                    var menuViewIndex = 0
                    menus.forEachIndexed { index, itemProp ->
                        val i = indexOfMenuItemViewByKey(itemProp.key, menuViewIndex)
                        val menuItemView = getChildAt(i) as? MenuCheckableItemView
                        val isChecked = prop.navigationKey == itemProp.key
                        if (menuItemView == null) {
                            val view = newMenuCheckableItemView(ctx, itemProp)
                            view.checked = isChecked
                            addView(
                                view,
                                menuViewIndex,
                                lParams(wrapContent, wrapContent) {
                                    verticalMargin = dip(10)
                                }
                            )
//                            view.startAnimation(translateMenuItemAniShow)
                            menuViewIndex++
                        } else {
                            menuItemView.prop = itemProp
                            menuItemView.checked = isChecked
                            if (i > menuViewIndex) {
                                removeViews(menuViewIndex, i - menuViewIndex)
                            }
                            menuViewIndex++
                        }
                    }
                    if (childCount > menuViewIndex) {
                        removeViews(
                            menuViewIndex,
                            childCount - menuViewIndex
                        )
                    }
                }
            } else {
                mNavigationMemuLayout.apply {
                    var menuViewIndex = 0
                    menus.forEachIndexed { index, itemProp ->
                        val i = indexOfMenuItemViewByKey(itemProp.key, menuViewIndex)
                        val menuItemView = getChildAt(i) as? MenuItemView
                        if (menuItemView == null) {
                            val view = newMenuItemView(ctx, itemProp)
                            addView(
                                view,
                                menuViewIndex,
                                lParams(matchParent, wrapContent)
                            )
//                            view.startAnimation(translateMenuItemAniShow)
                            menuViewIndex++
                        } else {
                            menuItemView.prop = itemProp
                            if (i > menuViewIndex) {
                                removeViews(menuViewIndex, i - menuViewIndex)
                            }
                            menuViewIndex++
                        }
                    }
                    if (childCount > menuViewIndex) {
                        removeViews(
                            menuViewIndex,
                            childCount - menuViewIndex
                        )
                    }
                }
            }

        }
    }

    private fun ViewGroup.indexOfMenuItemViewByKey(key: Int?, start: Int = 0): Int {
        if (start > childCount) return -1
        for (i in start until childCount) {
            val view = getChildAt(i) as? MenuItemView ?: return -1
            if (view.prop.key == key) {
                return i
            }
        }
        return -1
    }

    private fun newMenuItemView(
        context: Context,
        data: MenuItemPropInfo,
    ) = MenuItemView(context).apply {
        orientation = LinearLayout.HORIZONTAL
        minimumHeight = dip(40)
        setOnClickListener(menuItemClick)
        if (data.key == MenuKeys.back) {
            setOnLongClickListener(menuItemLongClick)
        }
        prop = data
    }

    private fun newMenuCheckableItemView(
        context: Context,
        data: MenuItemPropInfo,
    ) = MenuCheckableItemView(context).apply {
        orientation = LinearLayout.VERTICAL
        minimumWidth = dip(60)
        minimumHeight = dip(60)
        setOnClickListener(menuItemClick)
        if (data.key == MenuKeys.back) {
            setOnLongClickListener(menuItemLongClick)
        }
        themeColor = appBarView.themeColor
        prop = data
    }


    override fun updateTheme(color: Int, bgColor: Int) {
        lineView.backgroundColor = bgColor
        for (i in 0 until mNavigationMemuLayout.childCount) {
            val view = mNavigationMemuLayout.getChildAt(i)
            if (view is MenuCheckableItemView) {
                view.themeColor = color
            }
        }
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