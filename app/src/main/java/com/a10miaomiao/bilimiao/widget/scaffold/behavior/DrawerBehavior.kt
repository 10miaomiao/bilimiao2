package com.a10miaomiao.bilimiao.widget.scaffold.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView
import splitties.dimensions.dip
import kotlin.math.min

class DrawerBehavior(context: Context, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<View>(context, attrs) {

    private val maxWidth = context.dip(400)
    private var parentRef: ScaffoldView? = null
    private var viewRef: View? = null
    private var behaviorDelegate: DrawerBehaviorDelegate? = null

    init {
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {
//        child.layout(0, 0, parent.measuredWidth, parent.measuredHeight)
        this.viewRef = child
        if (parent is ScaffoldView) {
            this.parentRef = parent
            if (behaviorDelegate == null) {
                behaviorDelegate = DrawerBehaviorDelegate(parent, child)
            }
            if (parent.fullScreenPlayer) {
                child.layout(0, 0, 0, 0)
            } else {
                behaviorDelegate?.onLayoutChild()
            }
        } else {
            child.layout(0, 0, 0, 0)
        }
        return true
    }

    override fun onMeasureChild(
        parent: CoordinatorLayout,
        child: View,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ): Boolean {
        val measuredWidth = min(maxWidth, parent.measuredWidth)
        val measuredHeight = parent.measuredHeight
        val widthSpec = View.MeasureSpec.makeMeasureSpec(measuredWidth, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(measuredHeight, View.MeasureSpec.EXACTLY)
        child.measure(widthSpec, heightSpec)
        return true
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: View, ev: MotionEvent): Boolean {
        return behaviorDelegate?.onTouchEvent(ev) ?: true
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: View,
        ev: MotionEvent
    ): Boolean {
        return behaviorDelegate?.onInterceptTouchEvent(ev) ?: false
    }


    fun openDrawer() {
        behaviorDelegate?.openDrawer()
    }

    fun closeDrawer() {
        behaviorDelegate?.closeDrawer()
    }

    fun isDrawerOpen(): Boolean {
        return behaviorDelegate?.isDrawerOpen() ?: false
    }

}