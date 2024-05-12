package com.a10miaomiao.bilimiao.widget.scaffold.behavior

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.addListener
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView
import splitties.dimensions.dip
import kotlin.math.max
import kotlin.math.min


class PlayerBehavior : CoordinatorLayout.Behavior<View> {

    var playerX = -1
    var playerY = -1
    var playerHeight = 0
    var playertWidth = 0
    var minPadding = 0

    var windowInsets = Insets.NONE

    var height = 0
    var width = 0

    var dragAreaHeight = 0

    var isShowChild = false

    private var currentOrientation = ScaffoldView.VERTICAL

    private var behaviorDelegate: PlayerBehaviorDelegate? = null

    var parentRef: ScaffoldView? = null
    var viewRef: View? = null

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        playerHeight = context.dip(200)
        playertWidth = context.dip(300)

        minPadding = context.dip(10)
        dragAreaHeight = context.dip(30)
        init()
    }

    fun init() {

    }

    fun setWindowInsets(left: Int, top: Int, right: Int, bottom: Int) {
        windowInsets = Insets.of(
            max(minPadding, left),
            max(minPadding, top),
            max(minPadding, right),
            max(minPadding, bottom)
        )
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {
        this.viewRef = child
        if (parent is ScaffoldView) {
            this.parentRef = parent
            if (behaviorDelegate == null) {
                behaviorDelegate = PlayerBehaviorDelegate(
                    parent,
                    child,
                    object : PlayerBehaviorDelegate.Insets{
                        override val top get() = windowInsets.top
                        override val bottom get() = windowInsets.bottom
                        override val left get() = windowInsets.left + parentRef!!.appBarWidth
                        override val right get() = windowInsets.right
                    }
                )
            }

            behaviorDelegate?.onLayoutChild()

            // 显示隐藏动画控制
            if (parent.showPlayer && !isShowChild) {
                isShowChild = true
                child.translationX = 0f
                child.translationY = 0f
                startShowAnimation(child)
            } else if (!parent.showPlayer && isShowChild) {
                isShowChild = false
                startHideAnimation(child)
            }

        } else {
            child.layout(0, 0, 0, 0)
        }
        return true
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: View, ev: MotionEvent): Boolean {
        return behaviorDelegate?.onTouchEvent(ev) ?: true
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: View,
        ev: MotionEvent
    ): Boolean {
        return behaviorDelegate?.onInterceptTouchEvent(ev) ?: false
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        nestedScrollAxes: Int,
        type: Int
    ): Boolean {
        return parentRef?.orientation == ScaffoldView.VERTICAL
                && target.tag != false
                && nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        val parent = parentRef ?: return
        val contentView = parentRef?.focusContent ?: return
        if (dy > 0 && parent.showPlayer
            && parent.orientation== ScaffoldView.VERTICAL
        ) {
            val playerMinHeight = parent.smallModePlayerMinHeight
            if (parent.smallModePlayerCurrentHeight > playerMinHeight) {
                consumed[1] = dy
                val playerHeight = max(
                    parent.smallModePlayerCurrentHeight - dy,
                    playerMinHeight,
                )
                parent.smallModePlayerCurrentHeight = playerHeight
                updateLayout()
                child.requestLayout()
                contentView.translationY = (parent.playerSpaceHeight).toFloat()
            }
        }
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
        val parent = parentRef ?: return
        val contentView = parentRef?.focusContent ?: return
        if (dyConsumed > 0 && parent.showPlayer
            && parent.orientation== ScaffoldView.VERTICAL
        ) {
            val playerMinHeight = parent.smallModePlayerMinHeight
            if (parent.smallModePlayerCurrentHeight > playerMinHeight) {
                val playerHeight = max(
                    parent.smallModePlayerCurrentHeight - dyConsumed,
                    playerMinHeight,
                )
                parent.smallModePlayerCurrentHeight = playerHeight
                updateLayout()
                child.requestLayout()
                contentView.translationY = (parent.playerSpaceHeight).toFloat()
            }
        }
        if (dyUnconsumed < 0 && parent.showPlayer
            && parent.orientation== ScaffoldView.VERTICAL
        ) {
            val playerMaxHeight = parent.smallModePlayerMaxHeight
            if (parent.smallModePlayerCurrentHeight < playerMaxHeight) {
                consumed[1] = dyUnconsumed
                val playerHeight = min(
                    parent.smallModePlayerCurrentHeight - dyUnconsumed,
                    playerMaxHeight,
                )
                parent.smallModePlayerCurrentHeight = playerHeight
                updateLayout()
                child.requestLayout()
                contentView.translationY = (parent.playerSpaceHeight).toFloat()
            }
        }
    }

    fun holdUpPlayer() {
        behaviorDelegate?.holdUpTop()
    }

    fun holdDownPlayer(){
        behaviorDelegate?.holdDown()
    }

    fun updateLayout(){
        behaviorDelegate?.updateWindowSize()
    }

    /**
     * 显示播放器动画
     */
    private fun startShowAnimation(child: View) {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 200
            interpolator = AccelerateInterpolator()
            val contentView = parentRef?.takeIf {
                it.orientation == ScaffoldView.VERTICAL
            }?.content
            val childHeight = (playerHeight + windowInsets.top).toFloat()
            child.pivotY = 0f
            child.pivotX = child.width / 2f
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                child.alpha = value
                child.scaleX = value
                child.scaleY = value
                contentView?.translationY = value * childHeight
            }
            addListener(
                onEnd = {
                    contentView?.translationY = childHeight
                }
            )
        }.start()
    }

    /**
     * 隐藏播放器动画
     */
    private fun startHideAnimation(child: View) {
        ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 200
            interpolator = AccelerateInterpolator()
            val contentView = parentRef?.takeIf {
                it.orientation == ScaffoldView.VERTICAL
            }?.content
            val childHeight = (playerHeight + windowInsets.top).toFloat()
            child.pivotY = 0f
            child.pivotX = child.width / 2f
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                child.alpha = value
                child.scaleX = value
                child.scaleY = value
                contentView?.translationY = value * childHeight
            }
            addListener(
                onEnd = {
                    child.layout(0, 0, 0, 0)
                    contentView?.translationY = 0f
                }
            )
        }.start()
    }
}


