package com.a10miaomiao.bilimiao.widget.comm

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.a10miaomiao.bilimiao.widget.comm.behavior.AppBarBehavior
import com.a10miaomiao.bilimiao.widget.comm.behavior.ContentBehavior
import com.a10miaomiao.bilimiao.widget.comm.behavior.PlayerBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior
import splitties.dimensions.dip
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.material.hidden

class ScaffoldView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr) {

    companion object {
        const val HORIZONTAL = 2 // 横屏
        const val VERTICAL = 1 // 竖屏
    }

    var onPlayerChanged: ((show: Boolean) -> Unit)? = null

    var orientation = VERTICAL
        set(value) {
            if (field != value) {
                field = value
                this.appBar?.orientation = orientation
                requestLayout()
            }
        }

    var showPlayer = false
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
                onPlayerChanged?.invoke(field)
            }
        }
    var fullScreenPlayer = false
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
                onPlayerChanged?.invoke(true)
            }
        }

    var appBarHeight = dip(64)
    var appBarWidth = dip(100)
    var playerHeight = -3
    var playerWidth = -3
    var _playerHeight = dip(200)
    var _playerWidth = dip(300)

    var appBar: AppBarView? = null
    var appBarBehavior: AppBarBehavior? = null

    var content: View? = null
    var contentBehavior: ContentBehavior? = null

    var player: View? = null
    var playerBehavior: PlayerBehavior? = null

    var bottomSheet: View? = null
    var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    private var drawerLayout: DrawerLayout? = null
    var drawerFragment: Fragment? = null

    lateinit var mDragHelper: ViewDragHelper

    init {

    }

    fun initView() {
        var mDragOriLeft = 0
        var mDragOriTop = 0

        mDragHelper = ViewDragHelper.create(this, object : ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                return child == appBar

//                val dragEnable = mDra/gHelper.isEdgeTouched(ViewDragHelper.EDGE_TOP, pointerId)
//                if (dragEnable) {
//                    if (mHelper.isEdgeTouched(SwipeBackLayout.EDGE_LEFT, pointerId)) {
//                        mCurrentSwipeOrientation = SwipeBackLayout.EDGE_LEFT
//                    } else if (mHelper.isEdgeTouched(SwipeBackLayout.EDGE_RIGHT, pointerId)) {
//                        mCurrentSwipeOrientation = SwipeBackLayout.EDGE_RIGHT
//                    }
//                }
//                return dragEnable
            }

            override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
                super.onViewCaptured(capturedChild, activePointerId)
                mDragOriLeft = capturedChild.left
                mDragOriTop = capturedChild.top
            }

            //            override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
//                return 0
//            }
//
            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
//                return when {
////                    top < 0 -> 0
//                    top > maxTop -> maxTop
//                    else -> top
//                }
                return top
            }


            override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
                super.onViewReleased(releasedChild, xvel, yvel)
//                val top = if (yvel < 0) {
//                    0
//                } else {
//                    maxTop
//                }
                mDragHelper.settleCapturedViewAt(0, top)
                invalidate();
            }

            override fun getViewVerticalDragRange(child: View): Int {
                return 10
            }

            override fun onEdgeTouched(edgeFlags: Int, pointerId: Int) {
                super.onEdgeTouched(edgeFlags, pointerId)
//                DebugMiao.log("onEdgeTouched", edgeFlags)
            }
        })
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_TOP);
    }


    override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
        if (params is LayoutParams) {
            when (val behavior = params.behavior) {
                is AppBarBehavior -> {
                    if (child is AppBarView) {
                        child.orientation = orientation
                        this.appBar = child
                        this.appBarBehavior = behavior
                    }
                }
                is ContentBehavior -> {
                    this.content = child
                    this.contentBehavior = behavior
                }
                is PlayerBehavior -> {
                    this.player = child
                    this.playerBehavior = behavior
                }
                is BottomSheetBehavior -> {
                    this.bottomSheet = child
                    this.bottomSheetBehavior = behavior
                }
            }
            if (child is DrawerLayout) {
                this.drawerLayout = child
            }
        }
        super.addView(child, params)
    }

    fun isDrawerOpen(): Boolean {
        return drawerLayout?.isDrawerOpen(Gravity.LEFT) ?: false
    }

    fun openDrawer() {
        drawerLayout?.openDrawer(Gravity.LEFT)
    }

    fun closeDrawer() {
        drawerLayout?.closeDrawer(Gravity.LEFT)
    }

    fun slideUpBottomAppBar() {
        if (orientation == VERTICAL) {
            appBar?.let {
                appBarBehavior?.slideUp(it)
            }
        }
    }

    fun slideDownBottomAppBar() {
        if (orientation == VERTICAL) {
            appBar?.let {
                appBarBehavior?.slideDown(it)
            }
        }
    }

    inline fun lParams(
        width: Int = wrapContent,
        height: Int = wrapContent,
        initParams: LayoutParams.() -> Unit = {}
    ): LayoutParams {
//        contract { callsInPlace(initParams, InvocationKind.EXACTLY_ONCE) }
        return LayoutParams(width, height).apply(initParams)
    }

    class LayoutParams(width: Int, height: Int) : CoordinatorLayout.LayoutParams(width, height) {

    }

}