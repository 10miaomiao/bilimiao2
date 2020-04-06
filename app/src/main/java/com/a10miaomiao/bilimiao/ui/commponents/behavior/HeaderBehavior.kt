package com.a10miaomiao.bilimiao.ui.commponents.behavior


import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit


class HeaderBehavior : CoordinatorLayout.Behavior<View> {

    companion object{
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

    var viewRef: View? = null
    private var top = 0

    constructor() {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun init() {

    }

    fun show() {
        viewRef?.let {
            it.translationY = -it.measuredHeight.toFloat()
            it.visibility = View.VISIBLE
            it.parent?.requestLayout()
            it.animate().apply {
                setListener(showAnimatorListener)
                duration = 300
                translationY(0f)
            }.start()
        }
    }

    fun hide() {
        viewRef?.let {
//            it.translationY = -it.measuredHeight.toFloat()
            it.animate().apply {
                setListener(hideAnimatorListener)
                duration = 300
                translationY(-it.measuredHeight.toFloat())
            }.start()
        }
    }

    fun isShow() = viewRef?.visibility == View.VISIBLE

    private val showAnimatorListener = object : AnimatorListener {
        override fun onAnimationStart(animation: Animator) {

        }

        override fun onAnimationEnd(animation: Animator?) {

        }

        override fun onAnimationCancel(animation: Animator) {

        }

        override fun onAnimationRepeat(animation: Animator) {}
    }

    private val hideAnimatorListener = object : AnimatorListener {
        override fun onAnimationStart(animation: Animator) {

        }

        override fun onAnimationEnd(animation: Animator?) {
            viewRef?.visibility = View.GONE
            viewRef?.parent?.requestLayout()
        }

        override fun onAnimationCancel(animation: Animator) {

        }

        override fun onAnimationRepeat(animation: Animator) {}
    }



    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        child.layout(0, top, parent.measuredWidth, top + child.measuredHeight)
        this.viewRef = child
        return true
    }


}