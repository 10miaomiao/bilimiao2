package com.a10miaomiao.bilimiao2

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.createGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.fragment
import com.a10miaomiao.bilimiao2.R
import com.a10miaomiao.bilimiao2.widget.comm.AppBarView
import com.a10miaomiao.bilimiao2.widget.comm.ScaffoldView
import com.a10miaomiao.bilimiao2.widget.comm.behavior.AppBarBehavior
import com.a10miaomiao.bilimiao2.widget.comm.behavior.ContentBehavior
import com.a10miaomiao.bilimiao2.widget.comm.behavior.PlayerBehavior
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

class MainUi(override val ctx: Context) : Ui {

    @InternalSplittiesApi
    val mContainerView = inflate<FragmentContainerView>(R.layout.container_fragment) {
        backgroundColor = 0xFFF2F2F2L.toInt()
    }


    @InternalSplittiesApi
    val mAppBar = view<AppBarView> {
        prop = AppBarView.PropInfo(
            title = "标题",
            navigationIcon = resources.getDrawable(R.drawable.ic_back_24dp)
        )
    }

    val mPlayerLayout = frameLayout {
        backgroundColor = Color.BLACK
    }

    @InternalSplittiesApi
    override val root = view<ScaffoldView>() {
        orientation = resources.configuration.orientation
        backgroundColor = 0xFFF2F2F2L.toInt()

        add(
            mPlayerLayout to lParams {
                behavior = PlayerBehavior(ctx, null)
                width = wrapContent
                height = wrapContent
            },
            mContainerView to lParams {
                behavior = ContentBehavior(ctx, null)
                width = matchParent
                height = matchParent
            },
            mAppBar to lParams {
                behavior = AppBarBehavior(ctx, null)
                width = matchParent
                height = wrapContent
            },
        )

    }

}