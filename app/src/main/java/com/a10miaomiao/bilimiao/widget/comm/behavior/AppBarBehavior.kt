package com.a10miaomiao.bilimiao.widget.comm.behavior

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.annotation.Dimension
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.comm.AppBarView
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import splitties.dimensions.dip

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
        appBarHeight = context.dip(70)
        appBarWidth = context.dip(120)
        appBarMenuHeight = context.config.appBarMenuHeight
        init()
    }

    fun init() {

    }

    var parentRef: ScaffoldView? = null
    var viewRef: View? = null

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        val height = appBarHeight + child.paddingBottom
        val width = appBarWidth + child.paddingLeft
        if (parent is ScaffoldView) {
            val orientation = parent.orientation
            if (parent.fullScreenPlayer) {
                child.layout(0, 0, 0, 0)
            } else if (orientation == ScaffoldView.HORIZONTAL) {
                currentState = STATE_SCROLLED_UP
                child.translationY = 0f
                child.layout(0, 0, width, parent.measuredHeight)
                child.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                child.layoutParams.width = width
            } else if (orientation == ScaffoldView.VERTICAL) {
                child.layout(0, parent.measuredHeight - height, parent.measuredWidth, parent.measuredHeight)
                child.layoutParams.height = height
                child.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            }
            if (parent.appBarHeight != height
                || parent.appBarWidth != width) {
                parent.appBarHeight = height
                parent.appBarWidth = width
                parent.content?.requestLayout()
            }
            this.parentRef = parent
        } else {
            child.layout(0, parent.measuredHeight - height, parent.measuredWidth, parent.measuredHeight)
        }
        this.viewRef = child
        return true
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
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
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