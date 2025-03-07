package com.a10miaomiao.bilimiao.widget.scaffold.ui

import android.animation.*
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.scaffold.AppBarView
import com.a10miaomiao.bilimiao.widget.scaffold.MenuCheckableItemView
import com.a10miaomiao.bilimiao.widget.scaffold.MenuItemView
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.topPadding

class AppBarVerticalUi(
    override val ctx: Context,
    val appBarView: AppBarView,
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

    private fun View.getInAnim(): Animator {
        val trX = PropertyValuesHolder.ofFloat("translationX", 0f, 0f);
        val trY = PropertyValuesHolder.ofFloat("translationY", 100f, 0f);
        val trAlpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
        return ObjectAnimator.ofPropertyValuesHolder(this, trY, trAlpha, trX);
    }

    private fun View.getOutAnim(): Animator {
        val trX = PropertyValuesHolder.ofFloat("translationX", 0f, 0f)
        val trY = PropertyValuesHolder.ofFloat("translationY", 0f, -100f)
        val trAlpha = PropertyValuesHolder.ofFloat("alpha", 1f, 0f)
        return ObjectAnimator.ofPropertyValuesHolder(this, trY, trAlpha, trX)
    }

    val mNavigationMenuLayout = horizontalLayout {
        gravity = Gravity.CENTER_HORIZONTAL
        val layoutTransition = LayoutTransition()

        //View出現的動畫
        //出现动画对LinearLayout.addView(View child, int index, LayoutParams params)方法无效
        layoutTransition.setAnimator(LayoutTransition.APPEARING, getInAnim())
//        layoutTransition.setAnimator(LayoutTransition.CHANGE_APPEARING, getInAnim())
        //元素在容器中消失時需要動畫顯示
        layoutTransition.setAnimator(LayoutTransition.DISAPPEARING, getOutAnim())
//        layoutTransition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, getOutAnim())
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        layoutTransition.setDuration(400)
        layoutTransition.setInterpolator(LayoutTransition.DISAPPEARING, DecelerateInterpolator())
        setLayoutTransition(layoutTransition)

        topPadding = mTitleHeight
    }

    private val lineView = textView {
//        backgroundColor = ctx.config.colorSurfaceVariant
    }

    @OptIn(InternalSplittiesApi::class)
    private val mNavigationScroller = view<HorizontalScrollView> {
        scrollBarSize = 0
        addView(
            mNavigationMenuLayout, lParams {
                width = matchParent
                height = mMenuHeight + mTitleHeight
            }
        )
    }

    override val root = frameLayout {
        addView(mTitle, lParams {
            width = matchParent
            height = mTitleHeight
        })
        addView(
            mNavigationScroller,
            lParams {
                gravity = Gravity.CENTER_HORIZONTAL
                width = wrapContent
                height = wrapContent
            }
        )
        addView(lineView, lParams(matchParent, dip(1)))
    }


    override fun setProp(prop: AppBarView.PropInfo?) {
        if (prop != null) {
            mTitle.text = (prop.title ?: "").replace("\n", " ")
            val menus = mutableListOf<MenuItemPropInfo>()
            prop.navigationButtonIcon?.let {
                val buttonKey = prop.navigationButtonKey
                val buttonTitle = when(buttonKey) {
                    MenuKeys.menu -> "菜单"
                    else ->  "返回"
                }
                menus.add(
                    MenuItemPropInfo(
                        key = buttonKey,
                        title = buttonTitle,
                        iconDrawable = it
                    )
                )
            }
            prop.menus?.let {
                if (prop.isNavigationMenu) menus.addAll(it)
                else menus.addAll(it.reversed())
            }
            if (menus.isEmpty()) {
                mNavigationMenuLayout.removeAllViews()
            } else if (prop.isNavigationMenu) {
                // 导航栏
                mNavigationMenuLayout.apply {
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
                                lParams(wrapContent, matchParent) {
                                    horizontalMargin = dip(10)
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
                // 正常栏
                mNavigationMenuLayout.apply {
                    var menuViewIndex = 0
                    menus.forEachIndexed { index, prop ->
                        val i = indexOfMenuItemViewByKey(prop.key, menuViewIndex)
                        val menuItemView = getChildAt(i) as? MenuItemView
                        if (menuItemView == null) {
                            val view = newMenuItemView(ctx, prop)
                            addView(
                                view,
                                menuViewIndex,
                                lParams(wrapContent, matchParent)
                            )
                            view.startAnimation(translateMenuItemAniShow)
                            menuViewIndex++
                        } else {
                            menuItemView.prop = prop
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
        orientation = LinearLayout.VERTICAL
        minimumWidth = dip(60)
        setBackgroundResource(config.selectableItemBackgroundBorderless)
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
        setOnClickListener(menuItemClick)
        if (data.key == MenuKeys.back) {
            setOnLongClickListener(menuItemLongClick)
        }
        themeColor = appBarView.themeColor
        prop = data
    }

    override fun updateTheme(color: Int, bgColor: Int) {
        lineView.backgroundColor = bgColor
        for (i in 0 until mNavigationMenuLayout.childCount) {
            val view = mNavigationMenuLayout.getChildAt(i)
            if (view is MenuCheckableItemView) {
                view.themeColor = color
            }
        }
    }

    private val translateMenuItemAniShow = AnimationSet(false).apply {
        addAnimation(TranslateAnimation(
            0f, 0f,
            100f, 0f,
        ))
        addAnimation(AlphaAnimation(0f, 1f))
        duration = 400
        repeatMode = Animation.REVERSE
        interpolator = DecelerateInterpolator()
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
        duration = 400
        interpolator = DecelerateInterpolator()
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
        duration = 400
        interpolator = DecelerateInterpolator()
        setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                mNavigationMenuLayout.visibility = View.GONE
            }
            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    fun showMenu() {
        mNavigationMenuLayout.startAnimation(translateAniShow)
        mNavigationMenuLayout.visibility = View.VISIBLE
    }

    fun hideMenu() {
        mNavigationMenuLayout.startAnimation(translateAniHide)
    }
}