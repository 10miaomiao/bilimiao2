package com.a10miaomiao.bilimiao.widget.scaffold.behavior

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.RT
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.RB
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.LB
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.LT
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.MIDDLE
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import splitties.dimensions.dip
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class PlayerBehaviorDelegate(
    private val parent: ScaffoldView,
    private val playerView: View,
    private val windowInsets: Insets,
) : ViewDragHelper.Callback() {

    private val dragAreaHeight = parent.dip(30)
    private val minPadding = parent.dip(10)
    // 拖拽窗口边缘调整大小区域宽度
    private val dragWidth = parent.dip(20)

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
    private val sizeChangeSettle: Runnable = object : Runnable {
        override fun run() {
            if (dragger.continueSettling(true)) {
                ViewCompat.postOnAnimation(parent, this)
                onSizeChanging = true
            } else {
                onSizeChanging = false
            }
        }
    }
    private var onSizeChanging = false

    private var currentOrientation = ScaffoldView.VERTICAL

    private var playerX = -1
    private var playerY = -1
    private var height = 0
    private var width = 0
    private var playerHeight = 0
    private var playerWidth = 0

    private var _videoRatio = 0f
    private var _measuredWidth = 0
    private var _measuredHeight = 0

    private val fullScreenDraggable
        get() = parent.fullScreenDraggable && danmakuVideoPlayer?.getStartButtonVisibility() == false

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
        if (state == ViewDragHelper.STATE_DRAGGING && !parent.isHoldUpPlayer) {
            danmakuVideoPlayer?.showSmallDargBar()
        }
    }

    override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
        danmakuVideoPlayer?.hideSmallDargBar()
        val originWidth = playerWidth
        val originHeight = playerWidth
        if (parent.isHoldUpPlayer) {
            if(playerView.x.toInt() == playerX && playerView.y.toInt() == playerY) {
                //点击展开
                parent.playerViewSizeStatus = ScaffoldView.PlayerViewSizeStatus.NORMAL
                val expandX = (playerWidth - originWidth) / 2
                val expandY = (playerHeight - originHeight) / 2
                playerView.layout(playerView.x.toInt() - expandX,playerView.y.toInt() - expandY,playerView.x.toInt() + expandX,playerView.y.toInt() + expandY)
            } else {
                resetPosition(xvel,yvel)
            }
        } else {
            if(playerView.x.toInt() == playerX && playerView.y.toInt() == playerY){
                //若全屏可拖拽，点击
                if (fullScreenDraggable){
                    danmakuVideoPlayer?.clickUiToggle()
                }
            } else if (playerView.x < windowInsets.left - playerWidth * 1 / 4 ){
                //拉至左边缘
                parent.playerViewSizeStatus = ScaffoldView.PlayerViewSizeStatus.HOLD_UP
                playerX = windowInsets.left
            } else if (playerView.x > parent.measuredWidth - playerWidth * 3 / 4 - windowInsets.right) {
                //拉至右边缘
                parent.playerViewSizeStatus = ScaffoldView.PlayerViewSizeStatus.HOLD_UP
                playerX = parent.measuredWidth - playerWidth - windowInsets.right
            } else {
                //判断播放器视图位置状态
                val playerMiddleX = playerView.x + playerWidth / 2
                val playerMiddleY = playerView.y + playerHeight / 2
                val middleX = (parent.measuredWidth + windowInsets.left - windowInsets.right) / 2
                val middleY = (parent.measuredHeight + windowInsets.top - windowInsets.bottom) / 2
                if (playerMiddleX < middleX + parent.dip(120)
                    && playerMiddleX > middleX - parent.dip(120)
                    && playerMiddleY < middleY + parent.dip(120)
                    && playerMiddleY > middleY - parent.dip(120)
                )
                    parent.playerViewPlaceStatus = ScaffoldView.PlayerViewPlaceStatus.MIDDLE
                else {
                    parent.playerViewPlaceStatus =
                        if (playerMiddleX < middleX) {
                            if (playerMiddleY < middleY) {
                                ScaffoldView.PlayerViewPlaceStatus.LT
                            } else {
                                ScaffoldView.PlayerViewPlaceStatus.LB
                            }
                        } else {
                            if (playerMiddleY < middleY) {
                                ScaffoldView.PlayerViewPlaceStatus.RT
                            } else {
                                ScaffoldView.PlayerViewPlaceStatus.RB
                            }
                        }
                }
                resetPosition(xvel, yvel)
            }
        }
        dragger.settleCapturedViewAt(playerX, playerY)
        if(originWidth == playerWidth && originHeight == playerHeight){
            ViewCompat.postOnAnimation(parent,draggerSettle)
        } else {
            ViewCompat.postOnAnimation(parent,sizeChangeSettle)
        }
        super.onViewReleased(releasedChild, xvel, yvel)
    }

    /**
     * 挂起到右上↗
     */
    fun holdUpTop() {
        playerX =parent.measuredWidth - (parent.dip(parent.playerHoldShowArea) * sqrt(parent.playerVideoRatio)).toInt() - windowInsets.right
        playerY = windowInsets.top
        if (dragger.smoothSlideViewTo(playerView, playerX, playerY)) {
            ViewCompat.postInvalidateOnAnimation(parent)
        }
        ViewCompat.postOnAnimation(parent, object : Runnable {
            override fun run() {
                if (dragger.continueSettling(true)) {
                    ViewCompat.postOnAnimation(parent, this)
                } else {
                    parent.playerViewSizeStatus = ScaffoldView.PlayerViewSizeStatus.HOLD_UP
                }
            }
        })
    }

    /**
     * 控制小窗位置
     */
    private fun resetPosition(xvel: Float, yvel: Float){
        val measuredWidth = parent.measuredWidth
        val measuredHeight = parent.measuredHeight
        if(parent.isHoldUpPlayer){
            //一点点惯性
            val newX = playerView.x.toInt() + (xvel.absoluteValue * xvel / 15000).toInt()
            val newY = playerView.y.toInt() + (yvel.absoluteValue * yvel / 15000).toInt()
            playerX = if (newX < windowInsets.left) {
                windowInsets.left
            } else if (newX > measuredWidth - playerWidth - windowInsets.right) {
                measuredWidth - playerWidth - windowInsets.right
            } else {
                newX
            }
            playerY = if (newY < windowInsets.top) {
                windowInsets.top
            } else if (newY > measuredHeight - playerHeight - windowInsets.bottom) {
                measuredHeight - playerHeight - windowInsets.bottom
            } else {
                newY
            }
        } else {
            when(parent.playerViewPlaceStatus){
                LT -> {
                    playerX = windowInsets.left
                    playerY = windowInsets.top
                }
                RT -> {
                    playerX = measuredWidth - playerWidth - windowInsets.right
                    playerY = windowInsets.top
                }
                LB -> {
                    playerX = windowInsets.left
                    playerY = measuredHeight - playerHeight - windowInsets.bottom
                }
                RB -> {
                    playerX = measuredWidth - playerWidth - windowInsets.right
                    playerY = measuredHeight - playerHeight - windowInsets.bottom
                }
                MIDDLE -> {
                    playerX = (measuredWidth - windowInsets.right + windowInsets.left - playerWidth)/2
                    playerY = (measuredHeight - windowInsets.bottom + windowInsets.top - playerHeight)/2
                }
            }
        }
    }

    var startX = 0
    var startY = 0
    var draggingSide = NONE
    companion object {
        const val NONE = 0
        const val LEFT = 1
        const val TOP = 2
        const val RIGHT = 3
        const val BOTTOM = 4
    }
    fun changeSizeByHeight(newHeight :Int){
        val widthHeightRatio = parent.playerVideoRatio
        val originWidth = playerWidth
        var newArea = (newHeight * sqrt(widthHeightRatio)).toInt()
        newArea = (newArea / parent.resources.displayMetrics.density).toInt()
        if(parent.isHoldUpPlayer){
            newArea = min(newArea,300)
            newArea = max(newArea,100)
            parent.playerHoldShowArea = newArea
        } else {
            newArea = min(newArea,600)
            newArea = max(newArea,150)
            parent.playerSmallShowArea = newArea
        }
        updateWindowSize()
        //变化时顶点
        if(parent.isHoldUpPlayer) {
            when (draggingSide) {
                LEFT -> {
                    playerX += originWidth - playerWidth
                }
                RIGHT -> {
                }
                BOTTOM -> {
                    playerX += (originWidth - playerWidth) / 2
                }
            }
        }
    }
    fun changeSizeByWidth(newWidth :Int){
        val widthHeightRatio = parent.playerVideoRatio
        val originWidth = playerWidth
        var newArea = (newWidth / sqrt(widthHeightRatio)).toInt()
        newArea = (newArea / parent.resources.displayMetrics.density).toInt()
        if(parent.isHoldUpPlayer){
            newArea = min(newArea,300)
            newArea = max(newArea,100)
            parent.playerHoldShowArea = newArea
        } else {
            newArea = min(newArea,600)
            newArea = max(newArea,150)
            parent.playerSmallShowArea = newArea
        }
        updateWindowSize()
        //变化时顶点
        if(parent.isHoldUpPlayer) {
            when (draggingSide) {
                LEFT -> {
                    playerX += originWidth - playerWidth
                }
                RIGHT -> {
                }
                BOTTOM -> {
                    playerX += (originWidth - playerWidth) / 2
                }
            }
        }
    }
    fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        danmakuVideoPlayer?.touch(ev)
        if (parent.showPlayer
            && !parent.fullScreenPlayer
            && parent.orientation == ScaffoldView.HORIZONTAL
            && !parent.isDrawerOpen()
            && parent.getMaskViewVisibility() != View.VISIBLE
        ) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = ev.x.toInt()
                    startY = ev.y.toInt()
                    if(startX > playerView.x
                        && startY > playerView.y
                        && startX < playerView.x + playerWidth
                        && startY < playerView.y + playerHeight
                    ){
                        if(startY > playerView.y + playerHeight - dragWidth){
                            draggingSide = BOTTOM
                            danmakuVideoPlayer?.stopTouch = true
                        } else if (startY < playerView.y + dragAreaHeight){
                            draggingSide = TOP
                            if(fullScreenDraggable || parent.isHoldUpPlayer){
                                dragger.captureChildView(playerView, ev.getPointerId(ev.actionIndex))
                            } else if(startX < playerView.x + playerWidth - dragAreaHeight){
                                // 减去右侧挂起按钮宽度
                                dragger.captureChildView(playerView, ev.getPointerId(ev.actionIndex))
                            }
                        } else if (startX < playerView.x + dragWidth){
                            draggingSide = LEFT
                            danmakuVideoPlayer?.stopTouch = true
                        } else if (startX > playerView.x + playerWidth - dragWidth){
                            draggingSide = RIGHT
                            danmakuVideoPlayer?.stopTouch = true
                        } else if (fullScreenDraggable ||parent.isHoldUpPlayer){
                            draggingSide = NONE
                            dragger.captureChildView(playerView, ev.getPointerId(ev.actionIndex))
                        } else {
                            draggingSide = NONE
                        }
                    } else {
                        draggingSide = NONE
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (draggingSide != NONE && dragger.viewDragState != ViewDragHelper.STATE_DRAGGING) {
                        val endX = ev.x.toInt()
                        val endY = ev.y.toInt()
                        startX = endX
                        startY = endY
                        when(draggingSide){
                            LEFT -> {
                                if(parent.isHoldUpPlayer){
                                    changeSizeByWidth((playerView.x - endX).toInt() + playerWidth)
                                } else {
                                    when (parent.playerViewPlaceStatus) {
                                        RT, RB -> {
                                            changeSizeByWidth((playerView.x - endX).toInt() + playerWidth)
                                        }
                                        MIDDLE -> {
                                            changeSizeByWidth((playerView.x - endX).toInt() + playerWidth)
                                        }
                                        LB, LT -> {}
                                    }
                                }
                            }
                            BOTTOM -> {
                                if(parent.isHoldUpPlayer){
                                    changeSizeByHeight((endY - playerView.y).toInt())
                                } else {
                                    when (parent.playerViewPlaceStatus) {
                                        RT, LT ,MIDDLE -> {
                                            changeSizeByHeight((endY - playerView.y).toInt())
                                        }
                                        LB, RB -> {}
                                    }
                                }
                            }
                            RIGHT -> {
                                if(parent.isHoldUpPlayer){
                                    changeSizeByWidth((endX - playerView.x).toInt())
                                } else {
                                    when (parent.playerViewPlaceStatus) {
                                        LB, LT, MIDDLE -> {
                                            changeSizeByWidth((endX - playerView.x).toInt())
                                        }
                                        RT, RB -> {}
                                    }
                                }
                            }
                        }
                        onLayoutChild()
                        parent.updateContentLayout()
                        parent.content?.requestLayout()
                        parent.subContent?.requestLayout()
                    }

                }
                MotionEvent.ACTION_UP -> {
                    draggingSide = NONE
                    danmakuVideoPlayer?.stopTouch = false
                }
            }
        }
        return dragger.shouldInterceptTouchEvent(ev)
    }

    fun onTouchEvent(ev: MotionEvent): Boolean {
        dragger.processTouchEvent(ev)
        return true
    }

    fun updateWindowSize(){
        val widthHeightRatio = parent.playerVideoRatio
        val onSmallShowArea = parent.playerSmallShowArea
        if (parent.orientation == ScaffoldView.HORIZONTAL) {
            if (parent.isHoldUpPlayer) {
                playerHeight = (parent.dip(parent.playerHoldShowArea) / sqrt(widthHeightRatio)).toInt()
                playerWidth = (parent.dip(parent.playerHoldShowArea) * sqrt(widthHeightRatio)).toInt()
            } else {
                playerHeight = (parent.dip(onSmallShowArea) / sqrt(widthHeightRatio)).toInt()
                playerWidth = (parent.dip(onSmallShowArea) * sqrt(widthHeightRatio)).toInt()
            }
            //阻止窗口过窄
            if (playerWidth < parent.contentMinWidth && !parent.isHoldUpPlayer){
                val newShowAreaDip =
                    (parent.contentMinWidth) / sqrt(widthHeightRatio)
                playerHeight = (newShowAreaDip / sqrt(widthHeightRatio)).toInt()
                playerWidth = (newShowAreaDip * sqrt(widthHeightRatio)).toInt()
            }
            //防止参数设置过大超出屏幕上限  保证一列内容区域的宽度
            if (playerWidth > parent.measuredWidth - windowInsets.left - windowInsets.right - parent.contentMinWidth) {
                val newShowAreaDip =
                    (parent.measuredWidth - windowInsets.left - windowInsets.right - parent.contentMinWidth) / sqrt(widthHeightRatio)
                playerHeight = (newShowAreaDip / sqrt(widthHeightRatio)).toInt()
                playerWidth = (newShowAreaDip * sqrt(widthHeightRatio)).toInt()
            }
            if (playerHeight > parent.measuredHeight - windowInsets.top - windowInsets.bottom) {
                val newShowAreaDip =
                    (parent.measuredHeight - windowInsets.top - windowInsets.bottom) * sqrt(widthHeightRatio)
                playerHeight = (newShowAreaDip / sqrt(widthHeightRatio)).toInt()
                playerWidth = (newShowAreaDip * sqrt(widthHeightRatio)).toInt()
            }
        } else {
            if(parent.isFoldPlayer) {
                parent.smallModePlayerHeight = parent.smallModePlayerMinHeight
            } else {
                parent.smallModePlayerHeight = ceil(parent.measuredWidth / widthHeightRatio).toInt()
                //防止竖屏时超出屏幕下边缘
                if (parent.smallModePlayerHeight > parent.measuredHeight * 3 / 4) {
                    parent.smallModePlayerHeight = parent.measuredHeight * 3 / 4
                }
            }
            playerHeight = parent.smallModePlayerHeight
            playerWidth = parent.measuredWidth
        }
        resetPosition(0f,0f)
    }

    //计算窗口加上边框的大小，供内容区域用
    fun updateContent(){
        when(parent.playerViewPlaceStatus){
            LT -> {
                parent.playerSpaceHeight = playerHeight + windowInsets.top
                parent.playerSpaceWidth = playerWidth + windowInsets.left - parent.appBarWidth
            }
            RT -> {
                parent.playerSpaceHeight = playerHeight + windowInsets.top
                parent.playerSpaceWidth = playerWidth + windowInsets.left - parent.appBarWidth
            }
            LB -> {
                parent.playerSpaceHeight = playerHeight + windowInsets.bottom
                parent.playerSpaceWidth = playerWidth + windowInsets.left - parent.appBarWidth
            }
            RB -> {
                parent.playerSpaceHeight = playerHeight + windowInsets.bottom
                parent.playerSpaceWidth = playerWidth + windowInsets.right
            }
            MIDDLE -> {
                parent.playerSpaceHeight = playerHeight
                parent.playerSpaceWidth = playerWidth
            }
        }
        parent.setContentTopClip(windowInsets.top)
    }
    fun onLayoutChild() {
        if(onSizeChanging){
            //挂起和展开时，使播放器内容与窗口大小同步，并防止位置不同导致的动画闪烁
            playerView.layout(playerView.x.toInt(), playerView.y.toInt(), playerView.x.toInt() + playerWidth, playerView.y.toInt() + playerHeight)
            playerView.layoutParams.height = playerHeight
            playerView.layoutParams.width = playerWidth
            playerView.requestLayout()
            return
        } else {
            if (dragger.viewDragState == ViewDragHelper.STATE_SETTLING
                || dragger.viewDragState == ViewDragHelper.STATE_DRAGGING
            ) {
                return
            }
        }
        if(_measuredHeight != parent.measuredHeight
            ||_measuredWidth != parent.measuredWidth
            ||_videoRatio != parent.playerVideoRatio) {
            _measuredHeight = parent.measuredHeight
            _measuredWidth = parent.measuredWidth
            _videoRatio = parent.playerVideoRatio
            parent.updateLayout()
        }
        if (parent.fullScreenPlayer) {
            // 全屏
            onFullScreenLayoutChild()
        } else if (parent.orientation == ScaffoldView.HORIZONTAL) {
            // 横向屏幕
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
            width = playerWidth
            height = playerHeight
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