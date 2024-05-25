package com.a10miaomiao.bilimiao.widget.scaffold.behavior

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.LB
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.LT
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.MIDDLE
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.RB
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.RT
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
    private val dragWidth = parent.dip(12)
    private val holdButtonWidth
        get() = danmakuVideoPlayer?.getHoldButtonWidth() ?: 0

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


    private var playerX = -1
    private var playerY = -1
    private var height = 0
    private var width = 0
    private var playerHeight = 0
    private var playerWidth = 0

    private var _videoRatio = 0f
    private var _measuredWidth = 0
    private var _measuredHeight = 0


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
        if (state == ViewDragHelper.STATE_DRAGGING
            && !parent.isHoldUpPlayer
            && !parent.fullScreenDraggable) {
            danmakuVideoPlayer?.showSmallDargBar()
        }
    }

    override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
        danmakuVideoPlayer?.hideSmallDargBar()
        val originWidth = playerWidth
        val originHeight = playerWidth
        //加上惯性后
        val newX = playerView.x.toInt() + (xvel.absoluteValue * xvel / 15000).toInt()
        val newY = playerView.y.toInt() + (yvel.absoluteValue * yvel / 15000).toInt()
        if (parent.isHoldUpPlayer) {
            if(playerView.x.toInt() == playerX && playerView.y.toInt() == playerY) {
                //点击展开
                parent.playerViewSizeStatus = ScaffoldView.PlayerViewSizeStatus.NORMAL
                val expandX = (playerWidth - originWidth) / 2
                val expandY = (playerHeight - originHeight) / 2
                playerView.layout(playerView.x.toInt() - expandX,playerView.y.toInt() - expandY,playerView.x.toInt() + expandX,playerView.y.toInt() + expandY)
            } else {
                playerX = newX
                playerY = newY
            }
        } else {
            if(playerView.x.toInt() == playerX && playerView.y.toInt() == playerY){
            } else if (playerView.x < windowInsets.left - playerWidth * 1 / 4 ){
                //拉至左边缘
                parent.playerViewSizeStatus = ScaffoldView.PlayerViewSizeStatus.HOLD_UP
                playerView.layout(playerView.x.toInt() + originWidth - playerWidth,playerView.y.toInt(),playerView.x.toInt() + playerWidth,playerView.y.toInt() + playerHeight)
                playerX = windowInsets.left
                playerY = playerView.y.toInt()
            } else if (playerView.x > parent.measuredWidth - playerWidth * 3 / 4 - windowInsets.right) {
                //拉至右边缘
                parent.playerViewSizeStatus = ScaffoldView.PlayerViewSizeStatus.HOLD_UP
                playerX = parent.measuredWidth - playerWidth - windowInsets.right
                playerY = playerView.y.toInt()
            } else {
                //判断播放器视图位置状态
                val playerMiddleX = newX + playerWidth / 2
                val playerMiddleY = newY + playerHeight / 2
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
            }
        }
        resetPosition()
        dragger.settleCapturedViewAt(playerX, playerY)
        if(originWidth == playerWidth && originHeight == playerHeight){
            ViewCompat.postOnAnimation(parent,draggerSettle)
        } else {
            ViewCompat.postOnAnimation(parent,sizeChangeSettle)
        }
        super.onViewReleased(releasedChild, xvel, yvel)
    }

    /**
     * 展开窗口
     */
    fun holdDown(){
        if(!parent.showPlayer){
            return
        }
        val originHeight = playerHeight
        val originWidth = playerWidth
        parent.playerViewSizeStatus = ScaffoldView.PlayerViewSizeStatus.NORMAL
        val expandX = (playerWidth - originWidth) / 2
        val expandY = (playerHeight - originHeight) / 2
        playerView.layout(playerView.x.toInt() - expandX,playerView.y.toInt() - expandY,playerView.x.toInt() + expandX,playerView.y.toInt() + expandY)
        resetPosition()
        if (dragger.smoothSlideViewTo(playerView, playerX, playerY)) {
            ViewCompat.postInvalidateOnAnimation(parent)
        }
        ViewCompat.postOnAnimation(parent, sizeChangeSettle)
    }
    /**
     * 挂起到右上↗
     */
    fun holdUpTop() {
        if(!parent.showPlayer){
            return
        }
        val originHeight = playerHeight
        val originWidth = playerWidth
        parent.playerViewSizeStatus = ScaffoldView.PlayerViewSizeStatus.HOLD_UP
        playerX = parent.measuredWidth - playerWidth - windowInsets.right
        playerY = windowInsets.top
        // 动画起点x,y坐标
        val originX = playerView.x.toInt() + (originWidth - playerWidth) / 26
        val originY = playerView.y.toInt() + (originHeight - playerHeight) / 2
        // 在动画开始前改变布局位置及尺寸，在动画结束时就不会有顿挫感
        playerView.layout(originX, originY,originX + playerWidth,originY + playerHeight)
        // 开始动画
        if (dragger.smoothSlideViewTo(playerView, playerX, playerY)) {
            ViewCompat.postInvalidateOnAnimation(parent)
        }
        //用这个settle可以让视频内容一起缩放
        ViewCompat.postOnAnimation(parent,sizeChangeSettle)
    }

    /**
     * 控制小窗位置
     */
    private fun resetPosition() {
        val measuredWidth = parent.measuredWidth
        val measuredHeight = parent.measuredHeight
        if(parent.isHoldUpPlayer){
            playerX = if (playerX < windowInsets.left) {
                windowInsets.left
            } else if (playerX > measuredWidth - playerWidth - windowInsets.right) {
                measuredWidth - playerWidth - windowInsets.right
            } else {
                playerX
            }
            playerY = if (playerY < windowInsets.top) {
                windowInsets.top
            } else if (playerY > measuredHeight - playerHeight - windowInsets.bottom) {
                measuredHeight - playerHeight - windowInsets.bottom
            } else {
                playerY
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

    //手指落下的位置
    companion object {
        const val NONE = 0
        const val LEFTSIDE = 1
        const val TOPSIDE = 2
        const val RIGHTSIDE = 3
        const val BOTTOMSIDE = 4
        const val MIDAREA = 5
        const val HOLDBUTTON = 6
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
                LEFTSIDE -> {
                    playerX += originWidth - playerWidth
                }
                RIGHTSIDE -> {
                }
                else -> {
                    playerX += (originWidth - playerWidth) / 2
                }
            }
        }
        parent.requestLayout()
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
                LEFTSIDE -> {
                    playerX += originWidth - playerWidth
                }
                RIGHTSIDE -> {
                }
                else -> {
                    playerX += (originWidth - playerWidth) / 2
                }
            }
        }
        parent.requestLayout()
    }

    private var draggingSide = NONE
        set(value) {
            if(value != field){
                if(field == NONE){
                    //按下时
                    if (value in arrayOf(LEFTSIDE, RIGHTSIDE, BOTTOMSIDE)
                        && !parent.isHoldUpPlayer
                        && parent.subContentShown
                    ){
                        //开始改变大小，内容区域透明度下降
                        fun ContentBehavior.anim(){
                            animateAlpha(0.3f,300)
                        }
                        parent.contentBehavior?.anim()
                        parent.subContentBehavior?.anim()
                    }
                }
                if(value == NONE){
                    //松开时
                    if (field in arrayOf(LEFTSIDE, RIGHTSIDE, BOTTOMSIDE)
                        && !parent.isHoldUpPlayer
                        && parent.subContentShown
                    ){
                        fun ContentBehavior.anim(){
                            //先降至0
                            animateAlpha(0f,100){
                                //改变布局并恢复透明度
                                parent.updateLayout(false)
                                animateAlpha(1f,300)
                            }
                        }
                        parent.contentBehavior?.anim()
                        parent.subContentBehavior?.anim()
                    }
                }
                field = value
            }
        }
    private var startX = 0
    private var startY = 0
    fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
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
                    //down时拦截：四边区域（除去挂起按钮）、挂起后的中间区域
                    if(startX > playerView.x - dragWidth
                        && startY > playerView.y
                        && startX < playerView.x + playerWidth + dragWidth
                        && startY < playerView.y + playerHeight + dragWidth
                    ) {
                        if (startY > playerView.y + playerHeight - dragWidth) {
                            //下边缘
                            draggingSide = BOTTOMSIDE
                            return true
                        } else if (startY < playerView.y + dragAreaHeight) {
                            //顶部拖拽条
                            if (startX < playerView.x + playerWidth - holdButtonWidth || parent.isHoldUpPlayer) {
                                // 非挂起时，减去右侧挂起按钮宽度
                                draggingSide = TOPSIDE
                                dragger.captureChildView(playerView, ev.getPointerId(ev.actionIndex))
                                return dragger.shouldInterceptTouchEvent(ev)
                            } else {
                                //点到按钮
                                draggingSide = HOLDBUTTON
                                return false
                            }
                        } else if (startX < playerView.x + dragWidth) {
                            //左边缘
                            draggingSide = LEFTSIDE
                            return true
                        } else if (startX > playerView.x + playerWidth - dragWidth) {
                            //右边缘
                            draggingSide = RIGHTSIDE
                            return true
                        } else {
                            //中间区域
                            draggingSide = MIDAREA
                            if(parent.isHoldUpPlayer){
                                dragger.captureChildView(playerView, ev.getPointerId(ev.actionIndex))
                                return dragger.shouldInterceptTouchEvent(ev)
                            } else {
                                return false
                            }
                        }
                    } else {
                        draggingSide = NONE
                        return false
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    //move时拦截：非挂起&&中间区域&&手指移动&&全屏拖拽
                    //挂起按钮拖拽拦截

                    if ((startX - ev.x.toInt()).absoluteValue < parent.dip(10)
                        && (startY - ev.y.toInt()).absoluteValue < parent.dip(10)){
                        //手指移动微小时，不拦截触控
                        return false
                    }
                    if(draggingSide == HOLDBUTTON) {
                        val evCapture = ev.let {
                            it.action = MotionEvent.ACTION_DOWN
                            it
                        }
                        dragger.captureChildView(playerView, ev.getPointerId(ev.actionIndex))
                        dragger.shouldInterceptTouchEvent(evCapture)
                        dragger.processTouchEvent(evCapture)
                        return dragger.viewDragState == ViewDragHelper.STATE_DRAGGING
                    }
                    if(parent.isHoldUpPlayer || !parent.fullScreenDraggable){
                        return false
                    }
                    if(draggingSide == MIDAREA) {
                        val evCapture = ev.let {
                            it.action = MotionEvent.ACTION_DOWN
                            it
                        }
                        dragger.captureChildView(playerView, ev.getPointerId(ev.actionIndex))
                        dragger.shouldInterceptTouchEvent(evCapture)
                        dragger.processTouchEvent(evCapture)
                        return dragger.viewDragState == ViewDragHelper.STATE_DRAGGING
                    }
                }
                MotionEvent.ACTION_UP -> {
                    draggingSide = NONE
                }
                MotionEvent.ACTION_CANCEL -> {
                    draggingSide = NONE
                }
            }
        }
        return dragger.shouldInterceptTouchEvent(ev)
    }

    fun onTouchEvent(ev: MotionEvent): Boolean {
        if(dragger.viewDragState == ViewDragHelper.STATE_DRAGGING){
            dragger.processTouchEvent(ev)
            return true
        }
        when (ev.action){
            MotionEvent.ACTION_MOVE -> {
                if (draggingSide != NONE && dragger.viewDragState != ViewDragHelper.STATE_DRAGGING) {
                    val endX = ev.x.toInt()
                    val endY = ev.y.toInt()
                    when(draggingSide){
                        LEFTSIDE -> {
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
                        BOTTOMSIDE -> {
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
                        RIGHTSIDE -> {
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
                }
            }
            MotionEvent.ACTION_UP -> {
                draggingSide = NONE
            }
            MotionEvent.ACTION_CANCEL -> {
                draggingSide = NONE
            }
        }
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
            // 内容区域每列最小宽度，单屏模式时取0
            val contentMinWidth = if (parent.hasSubContent) {
                parent.contentMinWidth
            } else {
                0
            }
            //阻止窗口过窄
            if (playerWidth < contentMinWidth && !parent.isHoldUpPlayer){
                val newShowAreaDip =
                    (contentMinWidth) / sqrt(widthHeightRatio)
                playerHeight = (newShowAreaDip / sqrt(widthHeightRatio)).toInt()
                playerWidth = (newShowAreaDip * sqrt(widthHeightRatio)).toInt()
            }
            //防止参数设置过大超出屏幕上限  保证一列内容区域的宽度
            if (playerWidth > parent.measuredWidth - windowInsets.left - windowInsets.right - contentMinWidth) {
                val newShowAreaDip =
                    (parent.measuredWidth - windowInsets.left - windowInsets.right - contentMinWidth) / sqrt(widthHeightRatio)
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
            var maxHeight = ceil(parent.measuredWidth / widthHeightRatio).toInt()
            //防止竖屏时超出屏幕下边缘
            maxHeight = min(maxHeight,parent.measuredHeight * 3 / 4)
            maxHeight = max(maxHeight,parent.smallModePlayerMinHeight)
            parent.smallModePlayerMaxHeight = maxHeight
            playerHeight = parent.smallModePlayerCurrentHeight
            playerWidth = parent.measuredWidth
        }
        resetPosition()
        updateContent()
    }

    //计算窗口加上边框的大小，供内容区域用
    fun updateContent(){
        if(!parent.showPlayer){
            parent.playerSpaceHeight = 0
            parent.playerSpaceWidth = 0
        } else {
            if(parent.orientation == ScaffoldView.VERTICAL){
                parent.playerSpaceHeight = playerHeight + playerView.paddingTop
                parent.playerSpaceWidth = playerWidth
            } else {
                when(parent.playerViewPlaceStatus){
                    LT -> {
                        parent.playerSpaceHeight = playerHeight
                        parent.playerSpaceWidth = playerWidth + windowInsets.left - parent.appBarWidth
                    }
                    RT -> {
                        parent.playerSpaceHeight = playerHeight
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
            }
        }
    }
    fun onLayoutChild() {
        if(onSizeChanging){
            //挂起和展开时，使播放器内容与窗口大小同步，并防止位置不同导致的动画闪烁
            playerView.layout(playerView.x.toInt(), playerView.y.toInt(), playerView.x.toInt() + playerWidth, playerView.y.toInt() + playerHeight)
            playerView.layoutParams.height = playerHeight
            playerView.layoutParams.width = playerWidth
            playerView.requestLayout()
            return
        }
        if (dragger.viewDragState == ViewDragHelper.STATE_SETTLING
            || dragger.viewDragState == ViewDragHelper.STATE_DRAGGING
        ) {
            return
        }
        if(_measuredHeight != parent.measuredHeight
            ||_measuredWidth != parent.measuredWidth
            ||_videoRatio != parent.playerVideoRatio) {
            if(_videoRatio != parent.playerVideoRatio){
                parent.updateLayout(true)
            } else {
                parent.updateLayout(false)
            }
            _measuredHeight = parent.measuredHeight
            _measuredWidth = parent.measuredWidth
            _videoRatio = parent.playerVideoRatio
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
        // 播放器尺寸校正
        if (playerView.layoutParams.height != height || playerView.layoutParams.width != width) {
            playerView.layoutParams.height = height
            playerView.layoutParams.width = width
            playerView.requestLayout()
        }
    }

    /**
     * 全屏播放布局
     */
    private fun onFullScreenLayoutChild() {
        height = parent.measuredHeight
        width = parent.measuredWidth
        playerView.layout(0, 0, width, height)
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
            height = playerHeight + playerView.paddingTop
            width = playerWidth
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