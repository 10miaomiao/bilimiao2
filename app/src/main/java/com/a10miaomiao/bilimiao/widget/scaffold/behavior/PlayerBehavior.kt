package com.a10miaomiao.bilimiao.widget.scaffold.behavior

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
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
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


    // 显示动画
    private val showAnimation = AnimationSet(true).apply {
        addAnimation(
            ScaleAnimation(
                0.2f, 1f,
                0.1f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0f,
            )
        )
        addAnimation(
            AlphaAnimation(0f, 1f)
        )
        interpolator = DecelerateInterpolator()
        repeatMode = Animation.REVERSE
        duration = 200
    }

    // 隐藏动画
    private val hideAnimation = AnimationSet(true).apply {
        addAnimation(
            ScaleAnimation(
                1f, 0.2f,
                1f, 0.1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0f,
            )
        )
        addAnimation(
            AlphaAnimation(1f, 0f)
        )
        interpolator = AccelerateInterpolator()
        repeatMode = Animation.REVERSE
        duration = 200
    }

    private fun startShowAnimation(child: View) {
        child.startAnimation(showAnimation)
    }

    private fun startHideAnimation(child: View) {
        // 关闭动画
        child.startAnimation(hideAnimation)
        hideAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                child.layout(0, 0, 0, 0)
//                child.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }
}


