package com.a10miaomiao.bilimiao.widget.comm.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import splitties.dimensions.dip

class PlayerBehavior : CoordinatorLayout.Behavior<View> {

    var contentHeight = 0
    var contentWidth = 0

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        contentHeight = context.dip(200)
        contentWidth = context.dip(300)
        init()
    }

    var viewRef: View? = null

    fun init() {

    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        if (parent is ScaffoldView && parent.showPlayer) {
            val orientation = parent.orientation
            var height = 0
            var width = 0
            if (orientation == com.a10miaomiao.bilimiao.widget.comm.ScaffoldView.HORIZONTAL) {
                height = parent.measuredHeight
                width = contentWidth + child.paddingRight
                child.layout(parent.measuredWidth - width, 0, parent.measuredWidth, height);
            } else if (orientation == com.a10miaomiao.bilimiao.widget.comm.ScaffoldView.VERTICAL) {
                height = contentHeight + child.paddingTop
                width = parent.measuredWidth
                child.layout(0, 0, width, height);
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
            child.layout(0, 0, 0, 0);
        }

        this.viewRef = child
        return true
    }


}