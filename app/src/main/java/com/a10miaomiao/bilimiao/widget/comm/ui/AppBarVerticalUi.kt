package com.a10miaomiao.bilimiao.widget.comm.ui

import android.animation.*
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import com.a10miaomiao.bilimiao.comm.attr
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.recycler.*
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.comm.AppBarView
import com.a10miaomiao.bilimiao.widget.comm.MenuItemView
import splitties.dimensions.dip
import splitties.views.dsl.core.*
import splitties.views.imageDrawable

class AppBarVerticalUi(
    override val ctx: Context,
    val menuItemClick: View.OnClickListener,
    val menuItemLongClick: View.OnLongClickListener,
//    val backClick: View.OnClickListener,
//    val backLongClick: View.OnLongClickListener,
) : AppBarUi {

    val mTitleHeight = ctx.config.appBarTitleHeight
    val mMenuHeight = ctx.config.appBarMenuHeight

    val mTitle = textView {
        gravity = Gravity.CENTER
        textSize = 12f
        setTextColor(config.foregroundAlpha45Color)
    }

    val mNavigationIcon = imageView {
        setBackgroundResource(ctx.attr(android.R.attr.selectableItemBackgroundBorderless))
    }

//    val mNavigationIconLayout = frameLayout {
//        padding = dip(10)
//        addView(mNavigationIcon, lParams {
//            width = dip(24)
//            height = dip(24)
//        })
//    }

    private fun View.getInAnim(): Animator {
        val trX = PropertyValuesHolder.ofFloat("translationX", 0f, 0f);
        val trY = PropertyValuesHolder.ofFloat("translationY", 100f, 0f);
        val trAlpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
        return ObjectAnimator.ofPropertyValuesHolder(this, trY, trAlpha, trX);
    }

    private fun View.getOutAnim(): Animator {
        val trX = PropertyValuesHolder.ofFloat("translationX", 0f, 0f)
        val trY = PropertyValuesHolder.ofFloat("translationY", 0f, 100f)
        val trAlpha = PropertyValuesHolder.ofFloat("alpha", 1f, 0f)
        return ObjectAnimator.ofPropertyValuesHolder(this, trY, trAlpha, trX)
    }

    val mNavigationMemuLayout = horizontalLayout {
        gravity = Gravity.CENTER_HORIZONTAL
        val layoutTransition = LayoutTransition()

        //View出現的動畫
        layoutTransition.setAnimator(LayoutTransition.APPEARING, getInAnim())
        //元素在容器中消失時需要動畫顯示
        layoutTransition.setAnimator(LayoutTransition.DISAPPEARING, getOutAnim())
        setLayoutTransition(layoutTransition)

//        endPadding = dip(10)
    }

//    val mNavigationLayout = horizontalLayout {

//        addView(mNavigationIconLayout, lParams {
//            width = wrapContent
//            height = wrapContent
//        })
//        addView(mNavigationMemuLayout, lParams {
//            width = matchParent
//            height = matchParent
//            weight = 1f
//        })
//    }

//    val mNavigationLayout = recyclerView {
//        val lm = LinearLayoutManager(context)
//        lm.orientation = LinearLayoutManager.HORIZONTAL
//        scrollBarSize = 0
//        layoutManager = lm
//    }

    override val root = frameLayout {
        layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)

        addView(mTitle, lParams {
            width = matchParent
            height = mTitleHeight
        })
        addView(mNavigationMemuLayout, lParams {
            topMargin = mTitleHeight
            width = matchParent
            height = mMenuHeight
        })
    }


    override fun setProp(prop: AppBarView.PropInfo?) {
        if (prop != null) {
            if (prop.navigationIcon != null) {
//                mNavigationIconLayout.visibility = View.VISIBLE
                mNavigationIcon.imageDrawable = prop.navigationIcon
            } else {
//                mNavigationIconLayout.visibility = View.GONE
            }
            mTitle.text = (prop.title ?: "").replace("\n", " ")

            val menus = mutableListOf<MenuItemPropInfo>()
            prop.navigationIcon?.let {
                menus.add(
                    MenuItemPropInfo(
                        key = MenuKeys.back,
                        title = "返回",
                        iconResource = com.a10miaomiao.bilimiao.R.drawable.ic_back_24dp
//                        iconDrawable = it,
                    )
                )
            }
            prop.menus?.let { menus.addAll(it.reversed()) }
            if (menus.isEmpty()) {
                mNavigationMemuLayout.removeAllViews()
            } else {
                mNavigationMemuLayout.apply {
                    menus.forEachIndexed { index, menu ->
                        var menuItemView: MenuItemView
                        if (index >= childCount) {
                            menuItemView = MenuItemView(ctx)
                            menuItemView.orientation = LinearLayout.VERTICAL
                            menuItemView.minimumWidth = dip(60)
                            menuItemView.setBackgroundResource(config.selectableItemBackgroundBorderless)
                            menuItemView.setOnClickListener(menuItemClick)
                            menuItemView.setOnLongClickListener(menuItemLongClick)
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

    //向上位移显示动画  从自身位置的最下端向上滑动了自身的高度
    private val translateAniShow = TranslateAnimation(
        /* fromXType = */ Animation.RELATIVE_TO_SELF,
        /* fromXValue = */ 0f,
        /* toXType = */ Animation.RELATIVE_TO_SELF,
        /* toXValue = */ 0f,
        /* fromYType = */ Animation.RELATIVE_TO_SELF,
        /* fromYValue = */ 1f,
        /* toYType = */ Animation.RELATIVE_TO_SELF,
        /* toYValue = */ 0f
    ).apply {
        repeatMode = Animation.REVERSE
        duration = 200
    }

    //向下位移隐藏动画  从自身位置的最上端向下滑动了自身的高度
    private val translateAniHide = TranslateAnimation(
        /* fromXType = */ Animation.RELATIVE_TO_SELF,
        /* fromXValue = */ 0f,
        /* toXType = */ Animation.RELATIVE_TO_SELF,
        /* toXValue = */ 0f,
        /* fromYType = */ Animation.RELATIVE_TO_SELF,
        /* fromYValue = */ 0f,
        /* toYType = */ Animation.RELATIVE_TO_SELF,
        /* toYValue = */ 1f
    ).apply {
        repeatMode = Animation.REVERSE
        duration = 200
    }

    fun showMenu() {
        mNavigationMemuLayout.startAnimation(translateAniShow)
        mNavigationMemuLayout.visibility = View.VISIBLE
    }

    fun hideMenu() {
        mNavigationMemuLayout.startAnimation(translateAniHide)
        translateAniHide.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                mNavigationMemuLayout.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

    }
}