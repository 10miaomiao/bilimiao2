package com.a10miaomiao.bilimiao.widget.scaffold.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.RT
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.RB
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.LB
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.LT
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.MIDDLE

class ContentBehavior : CoordinatorLayout.Behavior<View> {

    constructor() {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun init() {
    }
    var isSub = false

    var parentRef: ScaffoldView? = null
    var viewRef: View? = null
    var downHeight = 0 // 界面下降高度
    var height = 0
    var width = 0
    var showPlayer = false

    var left = 0
    var top = 0
    var right = 0
    var bottom = 0

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        if (parent is ScaffoldView) {
            if(parentRef == null){
                parentRef = parent
                updateLayout()
            }
            val orientation = parent.orientation
            val playerWidth = parent.playerWidth
            val playerHeight = parent.playerHeight
            if (parent.fullScreenPlayer ) {
                height = 0
                width = 0
                child.layout(0, 0, 0, 0)
            } else {
                child.layout(left,top,right,bottom)

                if (orientation == ScaffoldView.VERTICAL) {
                    child.translationX = 0f
                    if (downHeight != playerHeight) {
                        downHeight = playerHeight
                        startDownAnimation(child, playerHeight.toFloat())
                    }
                } else {
                    child.translationX = 0f
                    child.translationY = 0f
                    downHeight = 0
                }
            }

            if (child.layoutParams.height != height || child.layoutParams.width != width) {
                child.layoutParams.height = height
                child.layoutParams.width = width
                child.requestLayout()
            }
        } else {
            child.layout(0, 0, parent.measuredWidth, parent.measuredHeight)
        }

        this.viewRef = child
        return true
    }

    private fun startDownAnimation(
        child: View,
        height: Float,
    ) {
        child.animate().apply {
            duration = 200
            translationY(height)
        }.start()
    }

    //触摸分配焦点
    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: View,
        ev: MotionEvent
    ): Boolean {
        if(ev.action == ACTION_DOWN){
            if (ev.x > left && ev.x < right && ev.y > top && ev.y < bottom) {
                child.requestFocus()
            }
        }
        return super.onInterceptTouchEvent(parent, child, ev)
    }

    //根据视频窗口分配内容区域
    fun updateLayout(){
        if(parentRef == null){
            return
        }
        //左侧右侧
        val isLeft = if(parentRef!!.contentExchanged) isSub else !isSub

        val isOnFocus = if(parentRef!!.focusOnMain) !isSub else isSub

        val mWidth = parentRef!!.measuredWidth
        val mHeight = parentRef!!.measuredHeight
        val pWidth = parentRef!!.playerSpaceWidth
        val pHeight = parentRef!!.playerSpaceHeight
        val iLeft = parentRef!!.appBarWidth
        val iRight = 0

        val originSplitRatio = parentRef!!.contentDefaultSplit
        val minWidth = parentRef!!.contentMinWidth
        val minHeight = parentRef!!.contentMinHeight

        var contentWidth = mWidth - iRight - iLeft
        var splitLeftWidth = (contentWidth * originSplitRatio).toInt()
        if(splitLeftWidth < minWidth)
            splitLeftWidth = minWidth
        if(contentWidth - splitLeftWidth < minWidth)
            splitLeftWidth = contentWidth - minWidth
        top = 0
        bottom = mHeight
        //竖屏
        if(parentRef!!.orientation == ScaffoldView.VERTICAL){
            if(isOnFocus){
                left = 0
                right = mWidth
            } else {
                left = 0
                right = 0
                bottom = 0
            }
            return onUpdateFinished(false)
        }
        //宽度不够分两列,或强制单区域
        if(contentWidth < minWidth*2 || !parentRef!!.showSubContent){
            if(isOnFocus){
                left = iLeft
                right = mWidth
            } else {
                left = 0
                right = 0
                bottom = 0
            }
            return onUpdateFinished(false)
        }
        //另一侧被过度挤压
        if(contentWidth - pWidth < minWidth){
            if(isLeft){
                left = iLeft
                right = iLeft + splitLeftWidth
            } else {
                left = iLeft + splitLeftWidth
                right = mWidth - iRight
            }
            return onUpdateFinished(true)
        }
        //挂起或无视频窗口
        if(parentRef!!.isHoldUpPlayer || !parentRef!!.showPlayer){
            if(isLeft){
                left = iLeft
                right = iLeft + splitLeftWidth
            } else {
                left = iLeft + splitLeftWidth
                right = mWidth - iRight
            }
            return onUpdateFinished(true)
        }
        //竖屏窄小窗
        if(mHeight - pHeight < minHeight){
            //宽度不够分两列
            if(contentWidth - pWidth < minWidth*2){
                //高度够分
                if(mHeight > 2 * minHeight){
                    when(parentRef!!.playerViewPlaceStatus) {
                        LT, LB -> {
                            if (isLeft) {
                                left = mWidth - contentWidth +pWidth
                                right = mWidth - iRight
                                top = 0
                                bottom = mHeight / 2
                            } else {
                                left = mWidth - contentWidth +pWidth
                                right = mWidth - iRight
                                top = mHeight / 2
                                bottom = mHeight
                            }
                        }
                        RT, RB -> {
                            if (isLeft) {
                                left = iLeft
                                right = mWidth - pWidth
                                top = 0
                                bottom = mHeight / 2
                            } else {
                                left = iLeft
                                right = mWidth - pWidth
                                top = mHeight / 2
                                bottom = mHeight
                            }
                        }
                        MIDDLE -> {
                            if (isLeft) {
                                left = iLeft
                                right = iLeft + splitLeftWidth
                            } else {
                                left = iLeft + splitLeftWidth
                                right = mWidth - iRight
                            }
                        }
                    }
                    return onUpdateFinished(true)
                }
                //都不够，按默认分割
                if(isLeft){
                    left = iLeft
                    right = iLeft + splitLeftWidth
                } else {
                    left = iLeft + splitLeftWidth
                    right = mWidth - iRight
                }
                return onUpdateFinished(true)
            }
            //宽度够分
            contentWidth -= pWidth
            splitLeftWidth = (contentWidth * originSplitRatio).toInt()
            if(splitLeftWidth < minWidth)
                splitLeftWidth = minWidth
            if(contentWidth - splitLeftWidth < minWidth)
                splitLeftWidth = contentWidth - minWidth
            when(parentRef!!.playerViewPlaceStatus){
                LT,LB -> {
                    if(isLeft){
                        left = iLeft + pWidth
                        right = iLeft + pWidth + splitLeftWidth
                    } else {
                        left = iLeft + pWidth + splitLeftWidth
                        right = mWidth - iRight
                    }
                }
                RT,RB -> {
                    if(isLeft){
                        left = iLeft
                        right = iLeft + splitLeftWidth
                    } else {
                        left = iLeft + splitLeftWidth
                        right = mWidth - iRight - pWidth
                    }
                }
                MIDDLE -> {
                    if(isLeft){
                        left = iLeft
                        right = iLeft + contentWidth/2
                    } else {
                        left = iLeft + contentWidth/2 + pWidth
                        right = mWidth - iRight
                    }
                }
            }
            return onUpdateFinished(true)
        }
        //正常小窗
        when(parentRef!!.playerViewPlaceStatus) {
            LT -> {
                if(isLeft){
                    left = iLeft
                    right = iLeft + pWidth
                    top += pHeight
                } else {
                    left = iLeft + pWidth
                    right = mWidth - iRight
                }
            }
            RT -> {
                if(isLeft){
                    left = iLeft
                    right = iLeft + contentWidth - pWidth
                } else {
                    left = iLeft + contentWidth - pWidth
                    right = mWidth - iRight
                    top += pHeight
                }
            }
            LB -> {
                if(isLeft){
                    left = iLeft
                    right = iLeft + pWidth
                    bottom -= pHeight
                } else {
                    left = iLeft + pWidth
                    right = mWidth - iRight
                }
            }
            RB -> {
                if(isLeft){
                    left = iLeft
                    right = iLeft + contentWidth - pWidth
                } else {
                    left = iLeft + contentWidth - pWidth
                    right = mWidth - iRight
                    bottom -= pHeight
                }
            }
            MIDDLE -> {
                if(isLeft){
                    left = iLeft
                    right = iLeft + splitLeftWidth
                } else {
                    left = iLeft + splitLeftWidth
                    right = mWidth - iRight
                }
            }
        }
        return onUpdateFinished(true)
    }
    fun onUpdateFinished(spaceForSubContent: Boolean){
        parentRef!!.spaceForSubContent = spaceForSubContent
        height = bottom - top
        width = right - left
        viewRef?.requestLayout()
    }
}