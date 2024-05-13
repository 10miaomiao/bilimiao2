package com.a10miaomiao.bilimiao.widget.scaffold.behavior

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.annotation.Dimension
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.scaffold.AppBarView
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView

class AppBarBehavior : CoordinatorLayout.Behavior<View> {

    private val ENTER_ANIMATION_DURATION = 225
    private val EXIT_ANIMATION_DURATION = 175
    private val STATE_SCROLLED_DOWN = 1
    private val STATE_SCROLLED_UP = 2
    private val FAST_OUT_LINEAR_IN_INTERPOLATOR = FastOutLinearInInterpolator()
    private val LINEAR_OUT_SLOW_IN_INTERPOLATOR = LinearOutSlowInInterpolator()

    private var currentState = STATE_SCROLLED_UP
    private var additionalHiddenOffsetY = 0
    private var currentAnimator: ViewPropertyAnimator? = null

    var appBarHeight = 0
    var appBarWidth = 0
    var appBarMenuHeight = 0
    var showPlayer = false


    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        appBarHeight = context.config.appBarHeight
        appBarWidth = context.config.appBarMenuWidth
        appBarMenuHeight = context.config.appBarMenuHeight
        init()
    }

    fun init() {

    }

    var parentRef: ScaffoldView? = null
    var viewRef: View? = null

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        this.viewRef = child
        if (parent is ScaffoldView) {
            this.parentRef = parent
            if (parent.fullScreenPlayer) {
                child.layout(0, 0, 0, 0)
            } else {
                if (currentState == STATE_SCROLLED_DOWN
                    && parent.orientation == ScaffoldView.HORIZONTAL) {
                    currentState = STATE_SCROLLED_UP
                    child.translationY = 0f
                }
                if (parent.orientation == ScaffoldView.HORIZONTAL) {
                    val width = appBarWidth + child.paddingLeft
                    child.layout(width - parent.measuredWidth, 0, width, parent.measuredHeight)
                } else {
                    val height = appBarHeight + child.paddingBottom
                    child.layout(
                        0,
                        parent.measuredHeight - height,
                        parent.measuredWidth,
                        parent.measuredHeight + parent.measuredHeight + height
                    )
                }
            }
        } else {
            val height = appBarHeight + child.paddingBottom
            val width = appBarWidth + child.paddingLeft
            child.layout(0, parent.measuredHeight - height, parent.measuredWidth, parent.measuredHeight)
        }
        return true
    }

    //触摸分配焦点
    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: View,
        ev: MotionEvent
    ): Boolean {
        var left = 0
        var right = 0
        var top = 0
        var bottom = 0
        if(ev.action == MotionEvent.ACTION_DOWN && parentRef != null){
            if (parentRef!!.fullScreenPlayer) {
            } else {
                if (parentRef!!.orientation == ScaffoldView.HORIZONTAL) {
                    val width = appBarWidth + child.paddingLeft
                    left = width - parentRef!!.measuredWidth
                    top = 0
                    right = width
                    bottom = parentRef!!.measuredHeight
                } else {
                    val height = appBarHeight + child.paddingBottom
                    left = 0
                    top = parentRef!!.measuredHeight - height
                    right = parentRef!!.measuredWidth
                    bottom = parentRef!!.measuredHeight + parentRef!!.measuredHeight + height
                }
            }
            if (ev.x > left && ev.x < right && ev.y > top && ev.y < bottom) {
                child.requestFocus()
            }
        }
        return super.onInterceptTouchEvent(parent, child, ev)
    }

    /**
     * Sets an additional offset for the y position used to hide the view.
     *
     * @param child the child view that is hidden by this behavior
     * @param offset the additional offset in pixels that should be added when the view slides away
     */
    fun setAdditionalHiddenOffsetY(child: View, @Dimension offset: Int) {
        additionalHiddenOffsetY = offset
        if (currentState == STATE_SCROLLED_DOWN) {
            child.translationY = (appBarMenuHeight + additionalHiddenOffsetY).toFloat()
        }
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        nestedScrollAxes: Int,
        type: Int
    ): Boolean {
        return viewRef?.top != 0 // AppBar在底部时
                && parentRef?.showMaskView != true // 未显示遮罩
                && target.tag != false // 目标View允许时，手动设置targetView的tag控制
                && nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        if (parentRef?.orientation == ScaffoldView.HORIZONTAL) {
            return
        }
        if (dyConsumed > 0) {
            slideDown(child)
        } else if (dyConsumed < 0) {
            slideUp(child)
        }
    }

    /** Returns true if the current state is scrolled up.  */
    private fun isScrolledUp(): Boolean {
        return currentState == STATE_SCROLLED_UP
    }

    /**
     * Performs an animation that will slide the child from it's current position to be totally on the
     * screen.
     */
    fun slideUp(child: View) {
        slideUp(child,  /*animate=*/true)
    }

    /**
     * Slides the child with or without animation from its current position to be totally on the
     * screen.
     *
     * @param animate `true` to slide with animation.
     */
    fun slideUp(child: View, animate: Boolean) {
        if (isScrolledUp()) {
            return
        }
        if (currentAnimator != null) {
            currentAnimator!!.cancel()
            child.clearAnimation()
        }
        currentState = STATE_SCROLLED_UP
        val targetTranslationY = 0
        if (animate) {
            animateChildTo(
                child,
                targetTranslationY,
                ENTER_ANIMATION_DURATION.toLong(),
                LINEAR_OUT_SLOW_IN_INTERPOLATOR
            )
        } else {
            child.translationY = targetTranslationY.toFloat()
        }
    }

    /** Returns true if the current state is scrolled down.  */
    private fun isScrolledDown(): Boolean {
        return currentState == STATE_SCROLLED_DOWN
    }

    /**
     * Performs an animation that will slide the child from it's current position to be totally off
     * the screen.
     */
    fun slideDown(child: View) {
        slideDown(child,  /*animate=*/true)
    }

    /**
     * Slides the child with or without animation from its current position to be totally off the
     * screen.
     *
     * @param animate `true` to slide with animation.
     */
    fun slideDown(child: View, animate: Boolean) {
        if (isScrolledDown()) {
            return
        }
        if (currentAnimator != null) {
            currentAnimator!!.cancel()
            child.clearAnimation()
        }
        currentState = STATE_SCROLLED_DOWN
        val targetTranslationY = appBarMenuHeight + additionalHiddenOffsetY
        if (animate) {
            animateChildTo(
                child,
                targetTranslationY,
                EXIT_ANIMATION_DURATION.toLong(),
                FAST_OUT_LINEAR_IN_INTERPOLATOR
            )
        } else {
            child.translationY = targetTranslationY.toFloat()
        }
    }

    private fun animateChildTo(
        child: View, targetY: Int, duration: Long, interpolator: TimeInterpolator
    ) {
        currentAnimator = child
            .animate()
            .translationY(targetY.toFloat())
            .setInterpolator(interpolator)
            .setDuration(duration)
            .setListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (child is AppBarView) {
                            if (targetY == 0){
                                child.showMenu()
                            } else {
                                child.hideMenu()
                            }
                        }
                        currentAnimator = null
                    }
                })
    }
}