package com.a10miaomiao.bilimiao.widget.layout

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import splitties.dimensions.dip
import splitties.views.assignAndGetGeneratedId
import splitties.views.dsl.constraintlayout.*
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.wrapContent

class DoubleColumnAutofitLayout@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var leftView: View? = null
        set(value) {
            field = value
            if (value?.id == null) {
                value?.assignAndGetGeneratedId()
            }
        }
    var rightView: View? = null
        set(value) {
            field = value
            if (value?.id == null) {
                value?.assignAndGetGeneratedId()
            }
            addView(value)
        }

    var expandWidth: Int = 0

    var contentViewId: Int? = null

    var dividerSize: Int = 0

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        var lView = leftView ?: return
        var cViewId = contentViewId ?: return
        var cView = findViewById<ViewGroup>(cViewId) ?: return
        if (w >= expandWidth) {
            if (cView.childCount > 0 && cView.getChildAt(0) == lView) {
                cView.removeViewAt(0)
            }
            if (childCount == 0 || getChildAt(0) != lView) {
                lView.layoutParams = lParams(dip(300), matchParent)
                addView(lView, 0)
                rightView?.layoutParams = lParams(0, matchParent) {
                    leftToRightOf(lView, dividerSize, 0)
                    rightToRight = parentId
                }
            }
        } else {
            if (childCount > 0 && getChildAt(0) == lView) {
                rightView?.layoutParams = lParams(matchParent, matchParent)
                removeViewAt(0)
            }
            if (cView.childCount == 0 || cView.getChildAt(0) != lView) {
                lView.layoutParams = MarginLayoutParams(matchParent, wrapContent).apply {
                    bottomMargin = dividerSize
                }
                cView.addView(lView, 0)
            }
        }
    }

//    override fun onViewAdded(view: View) {
//        val lp = view.layoutParams
//        if (lp is LayoutParams) {
//            if (lp.position == Position.LEFT) {
//                if (view.id == null) {
//                    assignAndGetGeneratedId()
//                }
//                leftView = view
//            } else if (lp.position == Position.RIGHT) {
//                rightView = view
//                super.onViewAdded(view)
//            }
//            super.onViewAdded(view)
//        } else {
//            super.onViewAdded(view)
//        }
//    }
//
//    override fun onFinishInflate() {
//        super.onFinishInflate()
//        DebugMiao.log("onFinishInflate")
//    }
//
//
//    inline fun lParams(
//        width: Int = matchConstraints,
//        height: Int = matchConstraints,
//        initParams: LayoutParams.() -> Unit = {}
//    ): LayoutParams {
//        return LayoutParams(width, height).apply(initParams).also { it.validate() }
//    }
//
//    class LayoutParams(width: Int, height: Int) : ConstraintLayout.LayoutParams(width, height) {
//        var position: Position = Position.UNKNOW
//    }
//
//    enum class Position{ UNKNOW, LEFT, RIGHT }

}