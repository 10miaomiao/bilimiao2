package com.a10miaomiao.bilimiao.widget.comm.behavior

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import splitties.dimensions.dip


class ContentBehavior : CoordinatorLayout.Behavior<View> {

    constructor() {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun init() {

    }

    var viewRef: View? = null
    var height = 0
    var width = 0
    var showPlayer = false

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        if (parent is ScaffoldView) {
//           ki
            val orientation = parent.orientation
            var playerWidth = 0
            var playerHeight = 0
            if (parent.showPlayer) {
                playerWidth = parent.playerWidth
                playerHeight = parent.playerHeight
            }
            if (parent.fullScreenPlayer) {
                height = 0
                width = 0
                child.layout(0, 0, 0, 0)
            } else if (orientation == ScaffoldView.HORIZONTAL) {
//                child.layout(parent.appBarWidth, 0, parent.measuredWidth - playerWidth, parent.measuredHeight)
//                height = parent.measuredHeight
//                width = parent.measuredWidth - parent.appBarWidth - playerWidth
                child.layout(parent.appBarWidth, 0, parent.measuredWidth, parent.measuredHeight)
                height = parent.measuredHeight
                width = parent.measuredWidth - parent.appBarWidth
            } else if (orientation == ScaffoldView.VERTICAL) {
                child.layout(0, playerHeight, parent.measuredWidth, parent.measuredHeight)
//                height = parent.measuredHeight - parent.appBarHeight - playerHeight
                height = parent.measuredHeight - playerHeight
                width = parent.measuredWidth
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

    fun playerChanged (parent: ScaffoldView, show: Boolean) {
        viewRef?.let {
            val animate = it.animate()
            animate.duration = 200
            animate.setListener(downAnimatorListener)
            if (parent.orientation == ScaffoldView.HORIZONTAL) {
                if (show) {
                    width = parent.measuredWidth - parent.appBarWidth - parent.playerWidth
                } else {
                    width = parent.measuredWidth - parent.appBarWidth
                }
//                animate.scaleX(
//                    width.toFloat() / it.layoutParams.width.toFloat()
//                )
                animate.translationX(
                    (width.toFloat() - it.layoutParams.width.toFloat()) / 4
                )
            } else if (parent.orientation == ScaffoldView.VERTICAL) {
                if (show) {
                    height = parent.measuredHeight - parent.appBarHeight - parent.playerHeight
                } else {
                    height = parent.measuredHeight - parent.appBarHeight
                }
//                animate.scaleY(
//                    height.toFloat() / it.layoutParams.height.toFloat()
//                )
                animate.translationY(
                    (it.layoutParams.height.toFloat() - height.toFloat()) / 4
                )
            }
            animate.start()
        }
    }

    private val downAnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {

        }

        override fun onAnimationEnd(animation: Animator) {
            viewRef?.let {
                if (it.layoutParams.height != height || it.layoutParams.width != width) {
                    it.layoutParams.height = height
                    it.layoutParams.width = width
//                    it.requestLayout()
                    val animate = it.animate()
                    animate.duration = 100
                    animate.setListener(upAnimatorListener)
                    animate.translationY(0f)
                    animate.translationX(0f)
                    animate.start()
                }
            }
        }

        override fun onAnimationCancel(animation: Animator) {

        }

        override fun onAnimationRepeat(animation: Animator) {}
    }

    private val upAnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
//            viewRef?.layoutParams?.height = height
//            viewRef?.requestLayout()
            viewRef?.requestLayout()
        }

        override fun onAnimationEnd(animation: Animator) {
//            headerViewRef?.visibility = View.GONE
        }

        override fun onAnimationCancel(animation: Animator) {

        }

        override fun onAnimationRepeat(animation: Animator) {}
    }


}