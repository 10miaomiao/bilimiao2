package com.a10miaomiao.bilimiao.widget.comm.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import splitties.dimensions.dip

class AppBarBehavior : CoordinatorLayout.Behavior<View> {

    var contentHeight = 0
    var contentWidth = 0
    var showPlayer = false

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        contentHeight = context.dip(64)
        contentWidth = context.dip(120)
        init()
    }

    fun init() {

    }

    var viewRef: View? = null

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        val height = contentHeight + child.paddingBottom
        val widht = contentWidth + child.paddingLeft
        if (parent is ScaffoldView) {
            val orientation = parent.orientation
            if (orientation == com.a10miaomiao.bilimiao.widget.comm.ScaffoldView.HORIZONTAL) {
                child.layout(0, 0, widht, parent.measuredHeight)
            } else if (orientation == com.a10miaomiao.bilimiao.widget.comm.ScaffoldView.VERTICAL) {
                child.layout(0, parent.measuredHeight - height, parent.measuredWidth, parent.measuredHeight)
            }
            if (parent.appBarHeight != height
                || parent.appBarWidth != widht) {
                parent.appBarHeight = height
                parent.appBarWidth = widht
                parent.content?.requestLayout()
            }
        } else {
            child.layout(0, parent.measuredHeight - height, parent.measuredWidth, parent.measuredHeight)
        }

        this.viewRef = child
        return true
    }


}