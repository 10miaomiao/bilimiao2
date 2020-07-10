package com.a10miaomiao.bilimiao.ui.commponents.behavior

import android.animation.Animator
import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import com.a10miaomiao.bilimiao.utils.DebugMiao

class ContainerBehavior : CoordinatorLayout.Behavior<View> {

    var viewRef: View? = null
    var headerViewRef: View? = null
    var height = -1

    constructor() {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun init() {

    }

    fun down(top: Float) {
        viewRef?.let {
            it.animate().apply {
                setListener(downAnimatorListener)
                duration = 300
                translationY(top)
            }.start()
        }
    }

    fun up(top: Float) {
        viewRef?.let {
            if (it.translationY == 0f) {
                return
            }
            it.animate().apply {
                setListener(upAnimatorListener)
                duration = 300
                translationY(0f)
            }.start()
        }
    }

    private val downAnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {

        }

        override fun onAnimationEnd(animation: Animator?) {
            viewRef?.layoutParams?.height = height
            viewRef?.requestLayout()
        }

        override fun onAnimationCancel(animation: Animator) {

        }

        override fun onAnimationRepeat(animation: Animator) {}
    }

    private val upAnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            viewRef?.layoutParams?.height = height
            viewRef?.requestLayout()
        }

        override fun onAnimationEnd(animation: Animator?) {
            headerViewRef?.visibility = View.GONE
        }

        override fun onAnimationCancel(animation: Animator) {

        }

        override fun onAnimationRepeat(animation: Animator) {}
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        this.viewRef = child
        var top = 0f
        for (i in 0 until parent.childCount) {
            val headerChild = parent.getChildAt(i)
            val lp = headerChild.layoutParams
            if (lp is CoordinatorLayout.LayoutParams && lp.behavior is HeaderBehavior) {
                this.headerViewRef = headerChild
                val behavior = lp.behavior
                if (behavior is HeaderBehavior) {
                    top = behavior.top
                }
            }
        }
        height = parent.measuredHeight - top.toInt()
        if (height != child.layoutParams.height) {
            if (top == 0f) {
                up(top)
            } else {
                down(top)
            }
        }
        return false
    }

}
