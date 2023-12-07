package com.a10miaomiao.bilimiao.widget.comm.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import java.lang.ref.WeakReference
import kotlin.math.abs

class DrawerBehavior : CoordinatorLayout.Behavior<View> {

    private var parentRef: ScaffoldView? = null
    private var viewRef: View? = null
    private var behaviorDelegate: DrawerBehaviorDelegate? = null

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun init() {

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