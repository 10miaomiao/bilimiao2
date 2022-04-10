package com.a10miaomiao.bilimiao.comm

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import splitties.views.dsl.core.Ui

abstract class MiaoUI : Ui {

    companion object {
        @PublishedApi internal val parentAndViews = arrayListOf<ViewsInfo>()
        @PublishedApi internal var isRecordViews = false
    }

    var parentView: View? = null

    open class ViewsInfo(
        private val viewGroup: ViewGroup,
        private val isRecord: Boolean,
    ) {
        val views = arrayListOf<View>()

        operator fun View.unaryPlus(): View {
            if (isRecord) {
                views.add(this)
            }
            return this
        }

        operator fun View.rangeTo(lParams: ViewGroup.LayoutParams): View {
            if (isRecord) {
                this.layoutParams = lParams
            }
            return this
        }

        open fun bindViews() {
            views.forEach {
                viewGroup.addView(it)
            }
        }

    }


    /**
     * 不知道起啥名
     */
    inline fun miao(block: () -> View): View {
        isRecordViews = true
        val view = block()
        parentAndViews.forEach {
            it.bindViews()
        }
        parentAndViews.clear()
        isRecordViews = false
        return view
    }

}