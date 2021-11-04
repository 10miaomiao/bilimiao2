package com.a10miaomiao.bilimiao

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentContainerView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.widget.comm.AppBarView
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.core.*

//class ViewGroupAndLayoutParams (val viewGroup: ViewGroup, val layoutParams: ViewGroup.LayoutParams)
//
//inline operator fun ViewGroup.plus(layoutParams: ViewGroup.LayoutParams): ViewGroupAndLayoutParams {
//    return ViewGroupAndLayoutParams(this, layoutParams)
//}
//
//inline operator fun ViewGroup.plus(view: View): View {
//    this.addView(view)
//    return view
//}
//
//inline operator fun ViewGroupAndLayoutParams.plus(view: View): View {
//    this.viewGroup.addView(view, this.layoutParams)
//    return view
//}
//
//rangeTo

inline fun ViewGroup.add(vararg views: View) {
    views.forEach {
        this.addView(it)
    }
}

inline fun ViewGroup.add(vararg views: Pair<View, ViewGroup.LayoutParams>) {
    views.forEach {
        this.addView(it.first, it.second)
    }
}

//inline infix fun View.
@OptIn(InternalSplittiesApi::class)
class MainUi(override val ctx: Context) : Ui {

    val mContainerView = inflate<FragmentContainerView>(R.layout.container_fragment) {
        backgroundColor = 0xFFF2F2F2L.toInt()
    }


    val mAppBar = view<AppBarView>{ }

    val mPlayerLayout = frameLayout {
        backgroundColor = Color.BLACK
    }

    override val root = view<ScaffoldView>() {
        orientation = resources.configuration.orientation
        backgroundColor = 0xFFF2F2F2L.toInt()

        add(
            mPlayerLayout to lParams {
                behavior = com.a10miaomiao.bilimiao.widget.comm.behavior.PlayerBehavior(ctx, null)
                width = wrapContent
                height = wrapContent
            },
            mContainerView to lParams {
                behavior = com.a10miaomiao.bilimiao.widget.comm.behavior.ContentBehavior(ctx, null)
                width = matchParent
                height = matchParent
            },
            mAppBar to lParams {
                behavior = com.a10miaomiao.bilimiao.widget.comm.behavior.AppBarBehavior(ctx, null)
                width = matchParent
                height = wrapContent
            },
        )

    }

}