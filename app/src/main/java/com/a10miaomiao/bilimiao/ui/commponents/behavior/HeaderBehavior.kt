package com.a10miaomiao.bilimiao.ui.commponents.behavior


import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import com.a10miaomiao.bilimiao.utils.DebugMiao
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit


class HeaderBehavior : CoordinatorLayout.Behavior<View> {

    companion object {
        fun <V : View> from(view: V): HeaderBehavior {
            val params = view.layoutParams
            return if (params !is CoordinatorLayout.LayoutParams) {
                throw IllegalArgumentException("The view is not a child of CoordinatorLayout")
            } else {
                val behavior = params.behavior
                if (behavior !is HeaderBehavior) {
                    throw IllegalArgumentException("The view is not associated with HeaderBehavior")
                } else {
                    behavior.viewRef = view
                    behavior
                }
            }
        }
    }

    constructor() {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun init() {

    }

    var viewRef: View? = null

    val height: Float get() = viewRef?.measuredHeight?.toFloat() ?: 0f
    var top = 0f

//    var isFull = false // 全屏
//        set(value) {
//            field = value
//            viewRef?.parent?.requestLayout()
//        }

    fun show() {
        viewRef?.let {
            it.visibility = View.VISIBLE
            top = height
            it.parent?.requestLayout()
        }
    }

    fun hide() {
        DebugMiao.log("hide")
        viewRef?.let {
            top = 0f
            it.parent?.requestLayout()
        }
    }

    fun isShow() = viewRef?.visibility == View.VISIBLE
            && top != 0f

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        child.layout(0, 0, parent.measuredWidth, child.measuredHeight)
        this.viewRef = child
        return true
    }


}