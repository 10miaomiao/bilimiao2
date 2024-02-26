package com.a10miaomiao.bilimiao.widget.comm.behavior

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegate2
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.google.android.material.transition.Hold
import splitties.dimensions.dip
import kotlin.math.ceil
import kotlin.math.sqrt

class PlayerBehaviorDelegate(
    private val parent: ScaffoldView,
    private val playerView: View,
    private val windowInsets: Insets,
) : ViewDragHelper.Callback() {

    private val dragAreaHeight = parent.dip(30)

    //    private val playerHeight = parent.dip(200)
//    private val playertWidth = parent.dip(300)
    private val minPadding = parent.dip(10)

    var viewSizeStatus = ScaffoldView.PlayerViewSizeStatus.NORMAL
    var widthHeightRatio = 0f
    //横屏时小屏播放区域，控制面积为其dip平方
    var onSmallShowArea = 0
    //挂起时面积
    val onHoldShowArea = 100

    @DragState
    var dragState = ViewDragHelper.STATE_IDLE
        private set

    private val danmakuVideoPlayer
        get() = playerView.let { it as? ViewGroup }
            ?.getChildAt(0)
            ?.let { it as? DanmakuVideoPlayer }

    private val draggerSettle: Runnable = object : Runnable {
        override fun run() {
            if (dragger.continueSettling(true)) {
                ViewCompat.postOnAnimation(parent, this)
            }
        }
    }

    private var currentOrientation = ScaffoldView.VERTICAL

    private var playerX = -1
    private var playerY = -1
    private var height = 0
    private var width = 0
    private var heightSmall = 0
    private var widthSmall = 0
    private var heightHold = 0
    private var widthHold = 0
    private var longSide = if (parent.orientation == ScaffoldView.HORIZONTAL)
        parent.measuredWidth
    else
        parent.measuredHeight
    private var shortSide = if (parent.orientation == ScaffoldView.HORIZONTAL)
        parent.measuredHeight
    else
        parent.measuredWidth

    private val dragger = ViewDragHelper.create(parent, 0.8f, this).apply {
    }

    override fun tryCaptureView(child: View, pointerId: Int): Boolean {
        return playerView === child
    }

    override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
        return left
    }

    override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
        return top
    }

    override fun onViewDragStateChanged(state: Int) {
        super.onViewDragStateChanged(state)
        dragState = state
        if (state == ViewDragHelper.STATE_DRAGGING) {
            danmakuVideoPlayer?.showSmallDargBar()
        }
    }

    override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
        super.onViewReleased(releasedChild, xvel, yvel)
        danmakuVideoPlayer?.hideSmallDargBar()
        if (parent.isHoldUpPlayer) {
            if (playerView.x > parent.measuredWidth - widthHold - windowInsets.right) {
                //拉至右边缘
                resetPosition(widthHold, heightHold)
            } else {
                holdStatusFalse()
            }
        } else {
            if (playerView.x > parent.measuredWidth - widthSmall * 3 / 4 - windowInsets.right) {
                //拉至右边缘
                holdStatusTrue()
            } else {
                resetPosition(widthSmall, heightSmall)
            }
        }
        dragger.settleCapturedViewAt(playerX, playerY)
        ViewCompat.postOnAnimation(parent, draggerSettle)
        super.onViewReleased(releasedChild, xvel, yvel)
    }

    fun holdStatusTrue() {
        parent.playerViewSizeStatus = ScaffoldView.PlayerViewSizeStatus.HOLD_UP
        resetPosition(widthHold, heightHold)
        playerX = parent.measuredWidth - widthHold - windowInsets.right
    }

    fun holdStatusFalse() {
        parent.playerViewSizeStatus = ScaffoldView.PlayerViewSizeStatus.NORMAL
        resetPosition(widthSmall, heightSmall)
    }

    /**
     * 拖拽超出屏幕，则左右吸边或上下吸边
     */
    private fun resetPosition(width: Int, height: Int) {
        val measuredWidth = parent.measuredWidth
        val measuredHeight = parent.measuredHeight
        playerX = if (playerView.x < windowInsets.left) {
            windowInsets.left
        } else if (playerView.x > measuredWidth - width - windowInsets.right) {
            measuredWidth - width - windowInsets.right
        } else {
            playerView.x.toInt()
        }
        playerY = if (playerView.y < windowInsets.top) {
            windowInsets.top
        } else if (playerView.y > measuredHeight - height - windowInsets.bottom) {
            measuredHeight - height - windowInsets.bottom
        } else {
            playerView.y.toInt()
        }
    }

    private fun resetPositionOnSizeChanged(
        width: Int,
        height: Int,
        measuredWidth: Int,
        measuredHeight: Int
    ) {
        playerX = if (playerX < windowInsets.left) {
            windowInsets.left
        } else if (playerX > measuredWidth - width - windowInsets.right) {
            measuredWidth - width - windowInsets.right
        } else {
            playerX
        }
        playerY = if (playerY < windowInsets.top) {
            windowInsets.top
        } else if (playerY > measuredHeight - height - windowInsets.bottom) {
            measuredHeight - height - windowInsets.bottom
        } else {
            playerY
        }
    }

    fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN
            && parent.showPlayer
            && !parent.fullScreenPlayer
            && parent.orientation == ScaffoldView.HORIZONTAL
        ) {
            val x = ev.x
            val y = ev.y
            if (playerView.x < x
                && playerView.y < y
                && playerView.x + width > x
                && playerView.y + (if (parent.isHoldUpPlayer) height else dragAreaHeight) > y
            ) {
                dragger.captureChildView(playerView, ev.getPointerId(ev.actionIndex))
            }
        }
        return dragger.shouldInterceptTouchEvent(ev)
    }

    fun onTouchEvent(ev: MotionEvent): Boolean {
        dragger.processTouchEvent(ev)
        return true
    }

    fun onLayoutChild() {
        if (dragState == ViewDragHelper.STATE_DRAGGING) {
            return
        }
        // 播放器视频长宽比
        val _videoRatio = parent.playerVideoRatio
        // 横屏时小屏播放区域，控制面积为其dip平方
        val _smallShowArea = parent.playerSmallShowArea
        // 竖屏时播放器视图状态
        val _viewSizeStatus = parent.playerViewSizeStatus
        //小窗口参数变化时，同步其他参数
        if (_videoRatio != widthHeightRatio
            || _smallShowArea != onSmallShowArea
            || _viewSizeStatus != viewSizeStatus
            || (shortSide != parent.measuredWidth && shortSide != parent.measuredHeight)
        ) {
            widthHeightRatio = _videoRatio
            onSmallShowArea = _smallShowArea
            viewSizeStatus = _viewSizeStatus
            val originLongSide = longSide
            val originWidthSmall = widthSmall
            val originWidthHold = widthHold
            //计算各参数的值
            longSide = if (parent.orientation == ScaffoldView.HORIZONTAL) {
                parent.measuredWidth
            } else {
                parent.measuredHeight
            }
            shortSide = if (parent.orientation == ScaffoldView.HORIZONTAL) {
                parent.measuredHeight
            } else {
                parent.measuredWidth
            }
            heightHold = (parent.dip(onHoldShowArea) / sqrt(widthHeightRatio)).toInt()
            widthHold = (parent.dip(onHoldShowArea) * sqrt(widthHeightRatio)).toInt()
            heightSmall = (parent.dip(onSmallShowArea) / sqrt(widthHeightRatio)).toInt()
            widthSmall = (parent.dip(onSmallShowArea) * sqrt(widthHeightRatio)).toInt()
            //防止参数设置过大超出屏幕上限
            if (widthSmall > longSide - windowInsets.left - windowInsets.right) {
                val newShowAreaDip =
                    (longSide - windowInsets.left - windowInsets.right) / sqrt(widthHeightRatio)
                heightSmall = (newShowAreaDip / sqrt(widthHeightRatio)).toInt()
                widthSmall = (newShowAreaDip * sqrt(widthHeightRatio)).toInt()
            }
            if (heightSmall > shortSide - windowInsets.top - windowInsets.bottom) {
                val newShowAreaDip =
                    (shortSide - windowInsets.top - windowInsets.bottom) * sqrt(widthHeightRatio)
                heightSmall = (newShowAreaDip / sqrt(widthHeightRatio)).toInt()
                widthSmall = (newShowAreaDip * sqrt(widthHeightRatio)).toInt()
            }
            //比例变化以右上角为基准
            if (height != 0 && width != 0 && playerX != -1) {
                playerX += longSide - originLongSide
                if (parent.isHoldUpPlayer) {
                    playerX += originWidthHold - widthHold
                } else {
                    playerX += originWidthSmall - widthSmall
                }
            }
            //xy初始值
            if (playerX == -1)
                playerX = longSide - windowInsets.right
            if (playerY == -1)
                playerY = windowInsets.top

            if (parent.isFoldPlayer) {
                parent.smallModePlayerHeight = parent.smallModePlayerMinHeight
            } else {
                parent.smallModePlayerHeight = ceil(shortSide / widthHeightRatio).toInt()
                //防止竖屏时超出屏幕下边缘
                if (parent.smallModePlayerHeight > longSide * 3 / 4) {
                    parent.smallModePlayerHeight = longSide * 3 / 4
                }
            }
            //防止参数变化时小窗超出屏幕边缘
            if (parent.isHoldUpPlayer) {
                resetPositionOnSizeChanged(widthHold, heightHold, longSide, shortSide)
            } else {
                resetPositionOnSizeChanged(widthSmall, heightSmall, longSide, shortSide)
            }
        }
        if (parent.fullScreenPlayer) {
            // 全屏
            onFullScreenLayoutChild()
        } else if (parent.orientation == ScaffoldView.HORIZONTAL) {
            // 横向屏幕
            if (parent.isHoldUpPlayer)
                onHoldUpLayoutChild()// 挂起小窗
            else
                onHorizontalScreenLayoutChild()
        } else {
            // 竖向屏幕
            onVerticalScreenLayoutChild()
        }

        if (parent.orientation != currentOrientation) {
            playerView.translationX = 0f
            playerView.translationY = 0f
        }
        currentOrientation = parent.orientation
        // 播放器尺寸校正
        if (playerView.layoutParams.height != height || playerView.layoutParams.width != width) {
            playerView.layoutParams.height = height
            playerView.layoutParams.width = width
            playerView.requestLayout()
        }
        val measuredHeight = playerView.measuredHeight
        val measuredWidth = playerView.measuredWidth
        // 内容区域布局尺寸校正
        if (parent.playerHeight != measuredHeight || parent.playerWidth != measuredWidth) {
            parent.playerHeight = measuredHeight
            parent.playerWidth = measuredWidth
            parent.playerX = playerX
            parent.playerY = playerY
            parent.content?.requestLayout()
        }
    }

    //小窗挂起布局
    private fun onHoldUpLayoutChild() {
        if (parent.showPlayer) {
            height = heightHold
            width = widthHold
            playerView.layout(playerX, playerY, playerX + width, playerY + height)
        } else {
            height = 0
            width = 0
        }
    }

    /**
     * 全屏播放布局
     */
    private fun onFullScreenLayoutChild() {
        height = parent.measuredHeight
        width = parent.measuredWidth
        playerView.layout(0, 0, width, height)
        playerView.translationX = 0f
        playerView.translationY = 0f
    }

    /**
     * 横向屏幕下布局
     */
    private fun onHorizontalScreenLayoutChild() {
        if (parent.showPlayer) {
            width = widthSmall
            height = heightSmall
            playerView.layout(playerX, playerY, playerX + width, playerY + height)
        } else {
            height = 0
            width = 0
        }
    }

    /**
     * 竖直屏幕下布局
     */
    private fun onVerticalScreenLayoutChild() {
        if (parent.showPlayer) {
            height = parent.smallModePlayerHeight + playerView.paddingTop
            width = parent.measuredWidth
            playerView.layout(0, 0, width, height)
        } else {
            height = 0
            width = 0
        }
    }

    /** @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @IntDef(
        ViewDragHelper.STATE_IDLE,
        ViewDragHelper.STATE_DRAGGING,
        ViewDragHelper.STATE_SETTLING,
    )
    @Retention(
        AnnotationRetention.SOURCE
    )
    annotation class DragState


    interface Insets {
        val top: Int
        val bottom: Int
        val left: Int
        val right: Int
    }


}