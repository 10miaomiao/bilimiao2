package com.a10miaomiao.bilimiao.widget.comm.behavior

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.comm.AppBarView
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import java.lang.ref.WeakReference
import kotlin.math.abs

class AppBarBehaviorDelegate(
    private val parent: ScaffoldView,
    private val appbarView: AppBarView,
) : ViewDragHelper.Callback() {

    private var initialY = 0f
    private var initialX = 0f

    private val appBarHeight = parent.context.config.appBarHeight
    private val appBarWidth = parent.context.config.appBarMenuWidth

    @DragState
    private var dragState = STATE_COLLAPSED

    @DragState
    private var targetState = STATE_COLLAPSED

    private var nestedScrollingChildRef: WeakReference<View>? = null
    private val draggerSettle: Runnable = object : Runnable {
        override fun run() {
            if (dragger.continueSettling(true)) {
                ViewCompat.postOnAnimation(parent, this)
            }
        }
    }

    private val dragger = ViewDragHelper.create(parent, this).apply {
    }

    init {
    }

    override fun tryCaptureView(child: View, pointerId: Int): Boolean {
        return child == appbarView
    }

    override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
        return if (parent.orientation == ScaffoldView.HORIZONTAL) {
            left
        } else {
            0
        }
    }

    override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
        return if (parent.orientation == ScaffoldView.VERTICAL) {
            top
        } else {
            0
        }
    }

    override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
        if (appbarView == changedView) {
            if (parent.orientation == ScaffoldView.VERTICAL) {
                val height = appbarView.measuredHeight - appbarView.paddingBottom
                val top = appbarView.top
                appbarView.setAalpha(top.toFloat() / height.toFloat())
            } else {
                val width = appbarView.measuredWidth - appbarView.paddingLeft
                val left = appbarView.left
                appbarView.setAalpha(-left.toFloat() / width.toFloat())
            }
        }
    }

    override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
        initialY = 0f
        initialX = 0f
        if (parent.orientation == ScaffoldView.VERTICAL) {
            if (yvel > 0) {
                val h = parent.measuredHeight
                targetState = STATE_COLLAPSED
                dragger.settleCapturedViewAt(0, h - appBarHeight - appbarView.paddingBottom)
            } else {
                targetState = STATE_EXPANDED
                dragger.settleCapturedViewAt(0, 0)
            }
        } else {
            if (xvel < 0) {
                val w = parent.measuredWidth
                targetState = STATE_COLLAPSED
                dragger.settleCapturedViewAt(appBarWidth + appbarView.paddingLeft - w, 0)
            } else {
                targetState = STATE_EXPANDED
                dragger.settleCapturedViewAt(0, 0)
            }
        }

        ViewCompat.postOnAnimation(parent, draggerSettle)
        super.onViewReleased(releasedChild, xvel, yvel)
    }

    fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (parent.orientation == ScaffoldView.VERTICAL) {
            val top = appbarView.top
            if (
                ev.action == MotionEvent.ACTION_DOWN
                && appbarView.translationY == 0f
                && ev.y > top
            ) {
                val initialY = ev.y
                val initialX = ev.x
                val scroll = nestedScrollingChildRef?.get()
                if (scroll != null && parent.isPointInChildBounds(
                        scroll,
                        initialX.toInt(),
                        initialY.toInt()
                    )
                ) {
                    return false
                }
                this.initialY = initialY
            } else if (
                ev.action == MotionEvent.ACTION_MOVE
                && initialY > 0f
                && abs(initialY - ev.y) > dragger.touchSlop
            ) {
                dragger.captureChildView(appbarView, ev.getPointerId(ev.actionIndex))
            } else if (
                ev.action == MotionEvent.ACTION_CANCEL
                || ev.action == MotionEvent.ACTION_UP
            ) {
                this.initialY = 0f
            }
        } else {
            val right = appbarView.right
            if (
                ev.action == MotionEvent.ACTION_DOWN
                && ev.x < right
            ) {
//                val initialY = ev.y
                val initialX = ev.x
//                val scroll = nestedScrollingChildRef?.get()
//                if (scroll != null && parent.isPointInChildBounds(scroll, initialX.toInt(), initialY.toInt())) {
//                    return false
//                }
                this.initialX = initialX
            } else if (
                ev.action == MotionEvent.ACTION_MOVE
                && initialX > 0f
                && abs(initialX - ev.x) > dragger.touchSlop
            ) {
                dragger.captureChildView(appbarView, ev.getPointerId(ev.actionIndex))
            } else if (
                ev.action == MotionEvent.ACTION_CANCEL
                || ev.action == MotionEvent.ACTION_UP
            ) {
                this.initialX = 0f
            }
        }

        return dragger.shouldInterceptTouchEvent(ev)
    }


    fun onTouchEvent(ev: MotionEvent): Boolean {
        dragger.processTouchEvent(ev)
        return true
    }

    override fun onViewDragStateChanged(state: Int) {
        super.onViewDragStateChanged(state)
        dragState = if (state == 0) {
            targetState
        } else {
            state
        }
        parent.changedDrawerState(dragState)
        if (dragState == STATE_COLLAPSED) {
            appbarView.setAalpha(1f)
        } else if (dragState == STATE_EXPANDED) {
            appbarView.setAalpha(0f)
        }
    }

    fun onLayoutChild(): Boolean {
        val orientation = parent.orientation
        if (dragState == STATE_COLLAPSED) {
            if (orientation == ScaffoldView.HORIZONTAL) {
                val width = appBarWidth + appbarView.paddingLeft
                appbarView.layout(width - parent.measuredWidth, 0, width, parent.measuredHeight)
            } else {
                val height = appBarHeight + appbarView.paddingBottom
                appbarView.layout(
                    0,
                    parent.measuredHeight - height,
                    parent.measuredWidth,
                    parent.measuredHeight + parent.measuredHeight + height
                )
            }
            appbarView.setAalpha(1f)
        } else if (dragState == STATE_EXPANDED) {
            appbarView.layout(0, 0, parent.measuredWidth, parent.measuredHeight)
            appbarView.setAalpha(0f)
        }

        nestedScrollingChildRef = WeakReference(findScrollingChild(appbarView))
        return true
    }

    @VisibleForTesting
    fun findScrollingChild(view: View): View? {
        if (ViewCompat.isNestedScrollingEnabled(view)) {
            return view
        }
        if (view is ViewGroup) {
            var i = 0
            val count = view.childCount
            while (i < count) {
                val scrollingChild = findScrollingChild(view.getChildAt(i))
                if (scrollingChild != null) {
                    return scrollingChild
                }
                i++
            }
        }
        return null
    }

    fun openDrawer() {
        targetState = STATE_EXPANDED
        dragger.smoothSlideViewTo(appbarView, 0, 0)
        ViewCompat.postOnAnimation(parent, draggerSettle)
    }

    fun closeDrawer() {
        if (parent.orientation == ScaffoldView.VERTICAL) {
            val h = parent.measuredHeight
            targetState = STATE_COLLAPSED
            dragger.smoothSlideViewTo(appbarView, 0, h - appBarHeight - appbarView.paddingBottom)
        } else {
            val w = parent.measuredWidth
            targetState = STATE_COLLAPSED
            dragger.smoothSlideViewTo(appbarView, appBarWidth + appbarView.paddingLeft - w, 0)
        }
        ViewCompat.postOnAnimation(parent, draggerSettle)
    }

    fun isDrawerOpen(): Boolean {
        return targetState == STATE_EXPANDED
    }

    companion object {
        const val STATE_DRAGGING = 1

        const val STATE_SETTLING = 2

        const val STATE_EXPANDED = 3

        const val STATE_COLLAPSED = 4

        /** @hide
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        @IntDef(
            STATE_EXPANDED,
            STATE_COLLAPSED,
            STATE_DRAGGING,
            STATE_SETTLING,
        )
        @Retention(
            AnnotationRetention.SOURCE
        )
        annotation class DragState
    }

}