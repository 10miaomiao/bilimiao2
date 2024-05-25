package com.a10miaomiao.bilimiao.widget.scaffold.behavior

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.View
import android.view.View.VISIBLE
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.LB
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.LT
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.MIDDLE
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.RB
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView.PlayerViewPlaceStatus.RT

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
    var height = 0
    var width = 0

    var endLeft = 0
    var endTop = 0
    var endRight = 0
    var endBottom = 0

    var left = 0
    var top = 0
    var right = 0
    var bottom = 0


    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {
        this.viewRef = child
        if (parent is ScaffoldView) {
            if (parentRef == null) {
                parentRef = parent
                updateLayout(false)
            }
            val orientation = parent.orientation
            if (parent.fullScreenPlayer) {
                height = 0
                width = 0
                child.layout(0, 0, 0, 0)
            } else {
                height = bottom - top
                width = right - left
                child.layout(left, top, right, bottom)
                if(orientation == ScaffoldView.VERTICAL){
                    val downHeight = parent.playerSpaceHeight
                    animateTranslationY(downHeight.toFloat())
                } else {
                    animateTranslationY(0f)
                    child.layout(left, top, right, bottom)
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
        return true
    }

    //触摸分配焦点
    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: View,
        ev: MotionEvent
    ): Boolean {
        if (parentRef?.isDrawerOpen() == true || parentRef?.getMaskViewVisibility() == VISIBLE) {
            return super.onInterceptTouchEvent(parent, child, ev)
        }
        if (ev.action == ACTION_DOWN) {
            if (ev.x > endLeft && ev.x < endRight && ev.y > endTop && ev.y < endBottom) {
                child.requestFocus()
            }
        }
        return super.onInterceptTouchEvent(parent, child, ev)
    }


    fun updateLayout(withAnimation: Boolean){
        if( _alphaAnimator?.isRunning == true
            && (left!=endLeft||top!=endTop||right!=endRight||bottom!=endBottom)
        ){
            //动画处于重新布局前的阶段，此时不打断原有动画，只更改end位置
            calculate()
            return
        }
        calculate()
        val duration = parentRef?.contentAnimationDuration
        if(withAnimation && duration != null && duration>0){
            animateAlpha(0f,duration/2){
                //播放完时 若透明度为0 更改布局位置并回复透明度
                if(viewRef?.alpha == 0f){
                    left = endLeft
                    top = endTop
                    right = endRight
                    bottom = endBottom
                    parentRef?.requestLayout()
                    animateAlpha(1f, duration / 2)
                }
            }
        } else {
            left = endLeft
            top = endTop
            right = endRight
            bottom = endBottom
        }
    }


    //根据视频窗口分配内容区域
    fun calculate() {
        val parentView = parentRef ?: return

        val isLeft = if (parentView.contentExchanged) isSub else !isSub

        val isOnFocus = if (parentView.focusOnMain) !isSub else isSub

        val mWidth = parentView.measuredWidth
        val mHeight = parentView.measuredHeight
        val pWidth = parentView.playerSpaceWidth
        val pHeight = parentView.playerSpaceHeight
        val iLeft = parentView.appBarWidth
        val iRight = 0

        val originSplitRatio = parentView.contentDefaultSplit
        val minWidth = parentView.contentMinWidth
        val minHeight = parentView.contentMinHeight

        var contentWidth = mWidth - iRight - iLeft
        var splitLeftWidth = (contentWidth * originSplitRatio).toInt()
        if (splitLeftWidth < minWidth)
            splitLeftWidth = minWidth
        if (contentWidth - splitLeftWidth < minWidth)
            splitLeftWidth = contentWidth - minWidth
        endTop = 0
        endBottom = mHeight
        // 竖屏
        if (parentView.orientation == ScaffoldView.VERTICAL) {
            if (isOnFocus) {
                endLeft = 0
                endRight = mWidth
            } else {
                endLeft = 0
                endRight = 0
                endBottom = 0
            }
            parentView.subContentShown = false
            return
        }
        // 未启用副屏
        if (!parentView.hasSubContent) {
            endLeft = iLeft
            endRight = mWidth
            parentView.subContentShown = false
            return
        }
        //宽度不够分两列,或强制单区域
        if (contentWidth < minWidth * 2 || !parentView.showSubContent) {
            if (isOnFocus) {
                endLeft = iLeft
                endRight = mWidth
            } else {
                endLeft = 0
                endRight = 0
                endBottom = 0
            }
            parentView.subContentShown = false
            return
        }
        //另一侧被过度挤压
        if (contentWidth - pWidth < minWidth) {
            if (isLeft) {
                endLeft = iLeft
                endRight = iLeft + splitLeftWidth
            } else {
                endLeft = iLeft + splitLeftWidth
                endRight = mWidth - iRight
            }
            parentView.subContentShown = true
            return
        }
        //挂起或无视频窗口
        if (parentView.isHoldUpPlayer || !parentView.showPlayer) {
            if (isLeft) {
                endLeft = iLeft
                endRight = iLeft + splitLeftWidth
            } else {
                endLeft = iLeft + splitLeftWidth
                endRight = mWidth - iRight
            }
            parentView.subContentShown = true
            return
        }
        //竖屏窄小窗
        if (mHeight - pHeight < minHeight) {
            //宽度不够分两列
            if (contentWidth - pWidth < minWidth * 2) {
                //高度够分
                if (mHeight > 2 * minHeight) {
                    when (parentView.playerViewPlaceStatus) {
                        LT, LB -> {
                            if (isLeft) {
                                endLeft = mWidth - contentWidth + pWidth
                                endRight = mWidth - iRight
                                endTop = 0
                                endBottom = mHeight / 2
                            } else {
                                endLeft = mWidth - contentWidth + pWidth
                                endRight = mWidth - iRight
                                endTop = mHeight / 2
                                endBottom = mHeight
                            }
                        }

                        RT, RB -> {
                            if (isLeft) {
                                endLeft = iLeft
                                endRight = mWidth - pWidth
                                endTop = 0
                                endBottom = mHeight / 2
                            } else {
                                endLeft = iLeft
                                endRight = mWidth - pWidth
                                endTop = mHeight / 2
                                endBottom = mHeight
                            }
                        }

                        MIDDLE -> {
                            if (isLeft) {
                                endLeft = iLeft
                                endRight = iLeft + splitLeftWidth
                            } else {
                                endLeft = iLeft + splitLeftWidth
                                endRight = mWidth - iRight
                            }
                        }
                    }
                    parentView.subContentShown = true
                    return
                }
                //都不够，按默认分割
                if (isLeft) {
                    endLeft = iLeft
                    endRight = iLeft + splitLeftWidth
                } else {
                    endLeft = iLeft + splitLeftWidth
                    endRight = mWidth - iRight
                }
                parentView.subContentShown = true
                return
            }
            //宽度够分
            contentWidth -= pWidth
            splitLeftWidth = (contentWidth * originSplitRatio).toInt()
            if (splitLeftWidth < minWidth)
                splitLeftWidth = minWidth
            if (contentWidth - splitLeftWidth < minWidth)
                splitLeftWidth = contentWidth - minWidth
            when (parentView.playerViewPlaceStatus) {
                LT, LB -> {
                    if (isLeft) {
                        endLeft = iLeft + pWidth
                        endRight = iLeft + pWidth + splitLeftWidth
                    } else {
                        endLeft = iLeft + pWidth + splitLeftWidth
                        endRight = mWidth - iRight
                    }
                }

                RT, RB -> {
                    if (isLeft) {
                        endLeft = iLeft
                        endRight = iLeft + splitLeftWidth
                    } else {
                        endLeft = iLeft + splitLeftWidth
                        endRight = mWidth - iRight - pWidth
                    }
                }

                MIDDLE -> {
                    if (isLeft) {
                        endLeft = iLeft
                        endRight = iLeft + contentWidth / 2
                    } else {
                        endLeft = iLeft + contentWidth / 2 + pWidth
                        endRight = mWidth - iRight
                    }
                }
            }
            parentView.subContentShown = true
            return
        }
        //正常小窗
        when (parentView.playerViewPlaceStatus) {
            LT -> {
                if (isLeft) {
                    endLeft = iLeft
                    endRight = iLeft + pWidth
                    endTop += pHeight
                } else {
                    endLeft = iLeft + pWidth
                    endRight = mWidth - iRight
                }
            }

            RT -> {
                if (isLeft) {
                    endLeft = iLeft
                    endRight = iLeft + contentWidth - pWidth
                } else {
                    endLeft = iLeft + contentWidth - pWidth
                    endRight = mWidth - iRight
                    endTop += pHeight
                }
            }

            LB -> {
                if (isLeft) {
                    endLeft = iLeft
                    endRight = iLeft + pWidth
                    endBottom -= pHeight
                } else {
                    endLeft = iLeft + pWidth
                    endRight = mWidth - iRight
                }
            }

            RB -> {
                if (isLeft) {
                    endLeft = iLeft
                    endRight = iLeft + contentWidth - pWidth
                } else {
                    endLeft = iLeft + contentWidth - pWidth
                    endRight = mWidth - iRight
                    endBottom -= pHeight
                }
            }

            MIDDLE -> {
                if (isLeft) {
                    endLeft = iLeft
                    endRight = iLeft + splitLeftWidth
                } else {
                    endLeft = iLeft + splitLeftWidth
                    endRight = mWidth - iRight
                }
            }
        }
        parentView.subContentShown = true
        return
    }

    private var _translationYAnimator: ValueAnimator? = null
    fun animateTranslationY(translationY: Float) {
        val child = viewRef ?: return
        val curTranslationY = child.translationY
        if (curTranslationY != translationY) {
            _translationYAnimator?.cancel()
            _translationYAnimator = ValueAnimator.ofFloat(
                curTranslationY,
                translationY
            ).apply {
                duration = 200
                addUpdateListener {
                    child.translationY = it.animatedValue as Float
                }
                start()
            }
        }
    }

    private var _alphaAnimator: ValueAnimator? = null
    fun animateAlpha(alpha:Float, duration:Int, onFinished: (() -> Unit)? = null){
        val child = viewRef ?: return
        val curAlpha = child.alpha
        if(curAlpha != alpha){
            _alphaAnimator?.cancel()
            _alphaAnimator = ValueAnimator.ofFloat(
                curAlpha,
                alpha
            ).apply {
                this.duration = duration.toLong()
                addUpdateListener {
                    child.alpha = it.animatedValue as Float
                }
                addListener(object : AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                    }
                    override fun onAnimationEnd(animation: Animator) {
                        onFinished?.invoke()
                    }
                    override fun onAnimationCancel(animation: Animator) {
                    }
                    override fun onAnimationRepeat(animation: Animator) {
                    }
                })
                start()
            }
        }
    }
}