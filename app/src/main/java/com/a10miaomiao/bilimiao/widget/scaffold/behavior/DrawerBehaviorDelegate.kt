package com.a10miaomiao.bilimiao.widget.scaffold.behavior

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import splitties.dimensions.dip
import java.lang.ref.WeakReference

class DrawerBehaviorDelegate(
    private val parent: ScaffoldView,
    private val drawerView: View,
) : ViewDragHelper.Callback() {

    //    private var initialY = 0f
    private var initialX = 0f
    private var touchOnAppBar = false
    private val minLeft = parent.dip(40)

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

    private val dragger = ViewDragHelper.create(parent, 0.8f, this).apply {
    }

    init {
    }

    override fun tryCaptureView(child: View, pointerId: Int): Boolean {
        return child == drawerView
    }

    override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
        return if (left > 0) 0 else left
    }

    override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
        return 0
    }

    override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
        if (drawerView == changedView) {
            val width = changedView.measuredWidth
            parent.setMaskViewAlpha((width + left).toFloat() / width.toFloat() * 0.4f)
        }
    }

    override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
        initialX = 0f
        val touchSlop = dragger.touchSlop * 5
        if (
            targetState == STATE_COLLAPSED
            && xvel > touchSlop
        ) {
            targetState = STATE_EXPANDED
        } else if (
            targetState == STATE_EXPANDED
            && xvel < -touchSlop
        ) {
            targetState = STATE_COLLAPSED
        }
        if (targetState == STATE_EXPANDED) {
            dragger.settleCapturedViewAt(0, 0)
        } else {
            val measuredWidth = releasedChild.measuredWidth
            dragger.settleCapturedViewAt(-measuredWidth, 0)
        }
        ViewCompat.postOnAnimation(parent, draggerSettle)
        super.onViewReleased(releasedChild, xvel, yvel)
    }


    fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (dragState == STATE_COLLAPSED && !parent.fullScreenPlayer) {
            // 折叠状态
            if (
                ev.action == MotionEvent.ACTION_DOWN
                && parent.bottomSheetState() == BottomSheetBehavior.STATE_HIDDEN
            ) {
                this.touchOnAppBar = (parent.orientation == ScaffoldView.VERTICAL
                        && ev.y > (parent.appBar?.top ?: parent.measuredHeight))
                        || (parent.orientation == ScaffoldView.HORIZONTAL
                        && ev.x < (parent.appBar?.right ?: 0))
                this.initialX = ev.x
            } else if (
                ev.action == MotionEvent.ACTION_MOVE
                && initialX > 0f
                && ev.x - initialX > dragger.touchSlop
                && (this.touchOnAppBar || this.initialX < minLeft)
            ) {
                this.initialX = 0f
                this.touchOnAppBar = false
                dragger.captureChildView(drawerView, ev.getPointerId(ev.actionIndex))
            } else if (
                ev.action == MotionEvent.ACTION_CANCEL
                || ev.action == MotionEvent.ACTION_UP
            ) {
                this.initialX = 0f
                this.touchOnAppBar = false
            }
        } else if (dragState == STATE_EXPANDED) {
            // 展开状态
            if (ev.action == MotionEvent.ACTION_DOWN) {
                this.initialX = ev.x
            } else if (
                ev.action == MotionEvent.ACTION_MOVE
                && initialX > 0f
                && initialX - ev.x > dragger.touchSlop
            ) {
                dragger.captureChildView(drawerView, ev.getPointerId(ev.actionIndex))
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
        if (dragState == STATE_DRAGGING) {
            drawerView.visibility = View.VISIBLE
            parent.setMaskViewVisibility(View.VISIBLE)
        } else if (dragState == STATE_COLLAPSED) {
            drawerView.visibility = View.INVISIBLE
            parent.setMaskViewVisibility(View.GONE)
        } else if (dragState == STATE_EXPANDED) {
            drawerView.visibility = View.VISIBLE
            parent.setMaskViewVisibility(View.VISIBLE)
        }
    }

    fun onLayoutChild(): Boolean {
        val measuredWidth = drawerView.measuredWidth
        val measuredHeight = drawerView.measuredHeight
        if (dragState == STATE_COLLAPSED) {
            drawerView.layout(-measuredWidth, 0, 0, measuredHeight)
        } else if (dragState == STATE_EXPANDED) {
            drawerView.layout(0, 0, measuredWidth, measuredHeight)
        }
        nestedScrollingChildRef = WeakReference(findScrollingChild(drawerView))
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
        drawerView.visibility = View.VISIBLE
        targetState = STATE_EXPANDED
        dragger.smoothSlideViewTo(drawerView, 0, 0)
        ViewCompat.postOnAnimation(parent, draggerSettle)
    }

    fun closeDrawer() {
        val measuredWidth = drawerView.measuredWidth
        targetState = STATE_COLLAPSED
        dragger.smoothSlideViewTo(drawerView, -measuredWidth, 0)
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