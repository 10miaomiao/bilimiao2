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
    var downHeight = 0 // 界面下降高度
    var height = 0
    var width = 0
    var showPlayer = false

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        if (parent is ScaffoldView) {
            val orientation = parent.orientation
            val playerWidth = parent.playerWidth
            val playerHeight = parent.playerHeight
            if (parent.fullScreenPlayer) {
                height = 0
                width = 0
                child.layout(0, 0, 0, 0)
            } else {
                val left = if (orientation == ScaffoldView.HORIZONTAL) parent.appBarWidth else 0
                child.layout(left, 0, parent.measuredWidth, parent.measuredHeight)
                height = parent.measuredHeight
                width = parent.measuredWidth - left

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


}