package com.a10miaomiao.bilimiao.widget.comm.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.Insets
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegate2
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import splitties.dimensions.dip
import kotlin.math.max

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

    var playerDelegate:PlayerDelegate2?=null


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
        if (parent is ScaffoldView) {
            if (behaviorDelegate == null) {
                behaviorDelegate = PlayerBehaviorDelegate(
                    parent,
                    child,
                    object : PlayerBehaviorDelegate.Insets{
                        override val top = windowInsets.top
                        override val bottom = windowInsets.bottom
                        override val left = windowInsets.left
                        override val right = windowInsets.right
                    }
                )
            }
            //播放器长宽比设置
            if(behaviorDelegate?.widthHeightRatio != (playerDelegate?.getVideoRatio() ?: (16f / 9f))) {
                behaviorDelegate?.widthHeightRatio = playerDelegate?.getVideoRatio() ?: (16f / 9f)
                if (behaviorDelegate?.widthHeightRatio == 0f)
                    behaviorDelegate?.widthHeightRatio = 16f / 9f
            }
            //横屏小窗面积设置
            if(behaviorDelegate?.onSmallShowArea != (playerDelegate?.getSmallShowArea() ?: 400))
                behaviorDelegate?.onSmallShowArea = playerDelegate?.getSmallShowArea() ?: 400

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


