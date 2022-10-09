package com.a10miaomiao.bilimiao.widget.comm.behavior

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.Insets
import androidx.core.os.postDelayed
import androidx.core.view.ViewCompat.offsetLeftAndRight
import androidx.core.view.ViewCompat.offsetTopAndBottom
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import splitties.dimensions.dip
import kotlin.math.max
import kotlin.math.roundToInt

class PlayerBehavior : CoordinatorLayout.Behavior<View> {

    var contentX = -1
    var contentY = -1
    var contentHeight = 0
    var contentWidth = 0
    var minPadding = 0

    var windowInsets = Insets.NONE

    var height = 0
    var width = 0

    var dragAreaHeight = 0

    private var currentOrientation = ScaffoldView.VERTICAL

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        contentHeight = context.dip(200)
        contentWidth = context.dip(300)
        minPadding = context.dip(10)
        dragAreaHeight = context.dip(30)
        init()
    }

    var viewRef: View? = null
    var parentRef: CoordinatorLayout? = null

    fun init() {

    }

    fun setWindowInsets (left: Int, top: Int, right: Int, bottom: Int) {
        windowInsets = Insets.of(
            max(minPadding, left),
            max(minPadding, top),
            max(minPadding, right),
            max(minPadding, bottom)
        )
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        if (parent is ScaffoldView && parent.showPlayer) {
            currentOrientation = parent.orientation
            if (parent.fullScreenPlayer) {
                height = parent.measuredHeight
                width = parent.measuredWidth
//                contentX = -1
//                contentY = -1
                child.layout(0, 0, width, height)
                child.translationX = 0f
                child.translationY = 0f
            } else if(!isMove) {
                if (currentOrientation == ScaffoldView.HORIZONTAL) {
                    height = contentHeight
                    width = contentWidth
//                    val parentWidth = parent.measuredWidth - windowInsets.left - windowInsets.right
//                    val parentHeight = parent.measuredHeight - windowInsets.top - windowInsets.bottom
//                    val left = (parentWidth - width) / 2 + windowInsets.left
//                    val top = (parentHeight - height) / 2 + windowInsets.top
//                    child.layout(left, top,  left + width, top + height)
                    val left = if (contentX == -1) {
                        parent.measuredWidth - windowInsets.right - width
                    } else { contentX }
                    val top =  if (contentY == -1) {
                        windowInsets.top
                    } else { contentY }
                    child.layout(left, top,  left + width, top + height)
                    child.translationX = 0f
                    child.translationY = 0f
//                    height = parent.measuredHeight
//                    width = contentWidth + child.paddingRight
//                    child.layout(parent.measuredWidth - width, 0, parent.measuredWidth, height);
                } else if (currentOrientation == ScaffoldView.VERTICAL) {
                    height = contentHeight + child.paddingTop
                    width = parent.measuredWidth
                    child.layout(0, 0, width, height)
//                    contentX = -1
//                    contentY = -1
                    child.translationX = 0f
                    child.translationY = 0f
                }
            }
            if (child.layoutParams.height != height || child.layoutParams.width != width) {
                child.layoutParams.height = height
                child.layoutParams.width = width
                child.requestLayout()
            }
            if (parent.playerHeight != height || parent.playerWidth != width) {
                parent.playerHeight = height
                parent.playerWidth= width
                parent.content?.requestLayout()
            }
        } else if (parent is ScaffoldView) {
            parent.playerHeight = 0
            parent.playerWidth= 0
//            contentX = -1
//            contentY = -1
            child.layout(0, 0, 0, 0)
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

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: View, event: MotionEvent): Boolean {
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
                    && child.y + dragAreaHeight > y) {
                    isMove = true
                    mDownX = x
                    mDownY = y
                    // 记录第一次在屏幕上坐标，用于计算初始位置
                    mFirstY = event.rawY.roundToInt()
                    mFirstX = event.rawX.roundToInt()
                }
                if (child is DanmakuVideoPlayer) {
                    child.showSmallDargBar()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isMove) {
                    offsetTopAndBottom(child, (y - mDownY).toInt())
                    offsetLeftAndRight(child, (x - mDownX).toInt())
                    contentX = child.x.toInt()
                    contentY = child.y.toInt()
                    mDownX = x
                    mDownY = y
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isMove) {
                    isMove = false
                    resetPosition(event, child)
                    if (child is DanmakuVideoPlayer) {
                        child.hideSmallDargBar()
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
            contentX = windowInsets.left
            child.animate().setInterpolator(DecelerateInterpolator())
                .setDuration(200)
                .x(contentX.toFloat())
                .start()
        } else if (child.x > measuredWidth - width - windowInsets.right){
            contentX = measuredWidth - width - windowInsets.right
            child.animate().setInterpolator(DecelerateInterpolator())
                .setDuration(200)
                .x(contentX.toFloat())
                .start()
        } else {
            contentX = child.x.toInt()
        }
        if (child.y < windowInsets.top) {
            contentY = windowInsets.top
            child.animate().setInterpolator(DecelerateInterpolator())
                .setDuration(200)
                .y(contentY.toFloat())
                .start()
        } else if (child.y > measuredHeight - height - windowInsets.bottom){
            contentY = measuredHeight - height - windowInsets.bottom
            child.animate().setInterpolator(DecelerateInterpolator())
                .setDuration(200)
                .y(contentY.toFloat())
                .start()
        } else {
            contentY = child.y.toInt()
        }
    }


}