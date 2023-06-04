package com.a10miaomiao.bilimiao.widget.comm.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat.offsetLeftAndRight
import androidx.core.view.ViewCompat.offsetTopAndBottom
import androidx.core.view.get
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import splitties.dimensions.dip
import kotlin.math.max
import kotlin.math.roundToInt

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

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        playerHeight = context.dip(200)
        playertWidth = context.dip(300)
        minPadding = context.dip(10)
        dragAreaHeight = context.dip(30)
        init()
    }

    var viewRef: View? = null
    var parentRef: CoordinatorLayout? = null

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

    /**
     * 全屏播放布局
     */
    private fun onFullScreenLayoutChild(
        parent: ScaffoldView,
        child: View,
    ) {
        height = parent.measuredHeight
        width = parent.measuredWidth
//      playerX = -1
//      playerY = -1
        child.layout(0, 0, width, height)
        child.translationX = 0f
        child.translationY = 0f
    }

    /**
     * 横向屏幕下布局
     */
    private fun onHorizontalScreenLayoutChild(
        parent: ScaffoldView,
        child: View,
    ) {
        if (parent.showPlayer) {
            height = playerHeight
            width = playertWidth
            val left = if (playerX == -1) {
                parent.measuredWidth - windowInsets.right - width
            } else {
                playerX
            }
            val top = if (playerY == -1) {
                windowInsets.top
            } else {
                playerY
            }
            child.layout(left, top, left + width, top + height)
        } else {
            height = 0
            width = 0
//            playerX = -1
//            playerY = -1
//            child.layout(0, 0, 0, 0)
        }
    }

    /**
     * 竖直屏幕下布局
     */
    private fun onVerticalScreenLayoutChild(
        parent: ScaffoldView,
        child: View,
    ) {
        if (parent.showPlayer) {
            height = playerHeight + child.paddingTop
            width = parent.measuredWidth
            child.layout(0, 0, width, height)
//            playerX = -1
//            playerY = -1
        } else {
            height = 0
            width = 0
//            playerX = -1
//            playerY = -1
//            child.layout(0, 0, 0, 0)
        }
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {
        if (isMove) {
            return true
        }
        val scaffoldView = parent as? ScaffoldView ?: return false

        if (scaffoldView.fullScreenPlayer) {
            // 全屏
            onFullScreenLayoutChild(scaffoldView, child)
        } else if (scaffoldView.orientation == ScaffoldView.HORIZONTAL) {
            // 横向屏幕
            onHorizontalScreenLayoutChild(scaffoldView, child)
        } else {
            // 竖向屏幕
            onVerticalScreenLayoutChild(scaffoldView, child)
        }
        if (scaffoldView.orientation != currentOrientation) {
            child.translationX = 0f
            child.translationY = 0f
        }
        currentOrientation = scaffoldView.orientation
        // 播放器尺寸校正
        if (child.layoutParams.height != height || child.layoutParams.width != width) {
            child.layoutParams.height = height
            child.layoutParams.width = width
            child.requestLayout()
        }
        // 内容区域布局尺寸校正
        if (parent.playerHeight != height || parent.playerWidth != width) {
            parent.playerHeight = height
            parent.playerWidth = width
            parent.content?.requestLayout()
        }
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
        this.viewRef = child
        this.parentRef = parent
        return true
    }

    private var mDownX = 0F
    private var mDownY = 0F
    private var mFirstY: Int = 0
    private var mFirstX: Int = 0
    private var isMove = false

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: View,
        event: MotionEvent
    ): Boolean {
        val x = event.x
        val y = event.y
        return parent is ScaffoldView && parent.showPlayer && !parent.fullScreenPlayer
                && currentOrientation == ScaffoldView.HORIZONTAL
                && child.x < x
                && child.y < y
                && child.x + width > x
                && child.y + dragAreaHeight > y
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: View, event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (child.x < x
                    && child.y < y
                    && child.x + width > x
                    && child.y + dragAreaHeight > y
                ) {
                    isMove = true
                    mDownX = x
                    mDownY = y
                    // 记录第一次在屏幕上坐标，用于计算初始位置
                    mFirstY = event.rawY.roundToInt()
                    mFirstX = event.rawX.roundToInt()
                }
//                if (child is DanmakuVideoPlayer) {
//                    child.showSmallDargBar()
//                }
                (child as? ViewGroup)?.getChildAt(0)?.let {
                    if (it is DanmakuVideoPlayer) it.showSmallDargBar()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isMove) {
                    offsetTopAndBottom(child, (y - mDownY).toInt())
                    offsetLeftAndRight(child, (x - mDownX).toInt())
                    playerX = child.x.toInt()
                    playerY = child.y.toInt()
                    mDownX = x
                    mDownY = y
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isMove) {
                    isMove = false
                    resetPosition(event, child)
//                    if (child is DanmakuVideoPlayer) {
//                        child.hideSmallDargBar()
//                    }
                    (child as? ViewGroup)?.getChildAt(0)?.let {
                        if (it is DanmakuVideoPlayer) it.hideSmallDargBar()
                    }
                }
            }
        }
        return true
    }

    /**
     * 拖拽超出屏幕，则左右吸边或上下吸边
     */
    private fun resetPosition(event: MotionEvent, child: View) {
        val measuredWidth = parentRef?.measuredWidth ?: 0
        val measuredHeight = parentRef?.measuredHeight ?: 0
        if (child.x < windowInsets.left) {
            playerX = windowInsets.left
            child.animate().setInterpolator(DecelerateInterpolator())
                .setDuration(200)
                .x(playerX.toFloat())
                .start()
        } else if (child.x > measuredWidth - width - windowInsets.right) {
            playerX = measuredWidth - width - windowInsets.right
            child.animate().setInterpolator(DecelerateInterpolator())
                .setDuration(200)
                .x(playerX.toFloat())
                .start()
        } else {
            playerX = child.x.toInt()
        }
        if (child.y < windowInsets.top) {
            playerY = windowInsets.top
            child.animate().setInterpolator(DecelerateInterpolator())
                .setDuration(200)
                .y(playerY.toFloat())
                .start()
        } else if (child.y > measuredHeight - height - windowInsets.bottom) {
            playerY = measuredHeight - height - windowInsets.bottom
            child.animate().setInterpolator(DecelerateInterpolator())
                .setDuration(200)
                .y(playerY.toFloat())
                .start()
        } else {
            playerY = child.y.toInt()
        }
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
//        showAnimation.setAnimationListener(object : Animation.AnimationListener {
//            override fun onAnimationStart(animation: Animation) {
//                child.visibility = View.VISIBLE
//            }
//
//            override fun onAnimationEnd(animation: Animation) {
//            }
//
//            override fun onAnimationRepeat(animation: Animation) {}
//        })
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


