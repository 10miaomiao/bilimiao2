package com.a10miaomiao.bilimiao.ui.commponents.behavior

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View

class ContainerBehavior : CoordinatorLayout.Behavior<View> {

    var viewRef: View? = null

    constructor() {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun init() {

    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        var top = 0
        for (i in 0 until parent.childCount) {
            val headerChild = parent.getChildAt(i)
            val lp = headerChild.layoutParams
            if (lp is CoordinatorLayout.LayoutParams && lp.behavior is HeaderBehavior) {
                if (headerChild.visibility == View.VISIBLE) {
                    top = parent.getChildAt(i).measuredHeight
                }
            }
        }
        child.layout(0, top, parent.measuredWidth, parent.measuredHeight)
        val height = parent.measuredHeight - top
        if(height != child.layoutParams.height){
            child.layoutParams.height = height
            child.requestLayout()
        }
        this.viewRef = child
        return true
    }

}
