package com.a10miaomiao.bilimiao.widget.comm.behavior

import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
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
    var widthHeightRatio = 0f
    //横屏时小屏播放区域，控制面积为其dip平方
    var onSmallShowArea= 0

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
        if(parent.holdUpPlayer){
            if(playerView.x > parent.measuredWidth - widthHold - windowInsets.right)//拉至右边缘
                resetPosition(widthHold,heightHold)
            else {
                parent.holdUpPlayer = false
                resetPosition(widthSmall,heightSmall)
            }
        } else {
            if(playerView.x > parent.measuredWidth - widthSmall*3/4 - windowInsets.right)//拉至右边缘
            {
                parent.holdUpPlayer = true
                resetPosition(widthHold,heightHold)
                playerX=parent.measuredWidth - widthHold - windowInsets.right
            } else {
                resetPosition(widthSmall,heightSmall)
            }
        }
        dragger.settleCapturedViewAt(playerX, playerY)
        ViewCompat.postOnAnimation(parent, draggerSettle)
        super.onViewReleased(releasedChild, xvel, yvel)
    }

    /**
     * 拖拽超出屏幕，则左右吸边或上下吸边
     */
    private fun resetPosition(width:Int,height:Int) {
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

    private fun resetPositionOnSizeChanged(width:Int,height:Int) {
        val measuredWidth = parent.measuredWidth
        val measuredHeight = parent.measuredHeight
        playerX = if (playerX< windowInsets.left) {
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
                && playerView.x + parent.measuredWidth > x
                && playerView.y + dragAreaHeight > y
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
        //小窗口参数变化时，同步小窗口参数
        if(widthHeightRatio!=parent.widthHeightRatio||onSmallShowArea!=parent.onSmallShowArea){
            parent.widthHeightRatio=widthHeightRatio
            parent.onSmallShowArea=onSmallShowArea
            //比例变化以右上角为基准
            if(height!=0 && width!=0) {
                if(parent.holdUpPlayer)
                    playerX += widthHold - (parent.dip(100) * sqrt(widthHeightRatio)).toInt()
                else
                    playerX += widthSmall - (parent.dip(onSmallShowArea) * sqrt(widthHeightRatio)).toInt()
            }
            heightHold=(parent.dip(100) / sqrt(widthHeightRatio)).toInt()
            widthHold=(parent.dip(100) * sqrt(widthHeightRatio)).toInt()
            if(onSmallShowArea != 0){
                heightSmall = (parent.dip(onSmallShowArea) / sqrt(widthHeightRatio)).toInt()
                widthSmall = (parent.dip(onSmallShowArea) * sqrt(widthHeightRatio)).toInt()
            }
            parent.smallModePlayerHeight= ceil(parent.measuredWidth/widthHeightRatio).toInt()
            //防止竖屏时超出屏幕下边缘
            if(parent.smallModePlayerHeight>parent.measuredHeight*3/4 && parent.smallModePlayerHeight>parent.measuredWidth*3/4)
                parent.smallModePlayerHeight=
                    if(parent.measuredHeight>parent.measuredWidth)
                        parent.measuredHeight*3/4
                    else
                        parent.measuredWidth*3/4
            //防止横屏时比例变化超出屏幕边缘
            if (parent.orientation == ScaffoldView.HORIZONTAL && height!=0 && width!=0) {
                if (parent.holdUpPlayer)
                    resetPositionOnSizeChanged(widthHold, heightHold)
                else
                    resetPositionOnSizeChanged(widthSmall, heightSmall)
            }
        }
        if (parent.fullScreenPlayer) {
            // 全屏
            onFullScreenLayoutChild()
        } else if (parent.orientation == ScaffoldView.HORIZONTAL) {
            // 横向屏幕
            if(parent.holdUpPlayer)
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
        // 内容区域布局尺寸校正
        if (parent.playerHeight != height || parent.playerWidth != width) {
            parent.playerHeight = height
            parent.playerWidth = width
            parent.content?.requestLayout()
        }
    }

    //小窗挂起布局
    private fun onHoldUpLayoutChild(){
        if (parent.showPlayer) {
            height=heightHold
            width=widthHold
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
            playerView.layout(left, top, left + width, top + height)
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
            width=widthSmall
            height=heightSmall
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
            playerView.layout(left, top, left + width, top + height)
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