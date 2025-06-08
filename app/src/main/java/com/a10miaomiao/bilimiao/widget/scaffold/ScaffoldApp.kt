package com.a10miaomiao.bilimiao.widget.scaffold

import android.app.Activity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewGroup
import androidx.core.view.size
import androidx.fragment.app.Fragment

fun <T: View> getView(viewGroup: ViewGroup, clazz: Class<T>): T? {
    for (i in 0 until viewGroup.size) {
        val view = viewGroup.getChildAt(i)
        if (view::class.java == clazz) {
            return view as T
        } else if (view is ViewGroup) {
            val v = getView(view, clazz)
            if (v != null) {
                return v
            }
        }
    }
    return null
}

fun Activity.getScaffoldView(): ScaffoldView {
    val rootView = findViewById<ViewGroup>(android.R.id.content)
    return getView(rootView, ScaffoldView::class.java)
        ?: throw Exception("NOT ScaffoldView")
}

fun Activity.getAppBarView(): AppBarView {
    val rootView = findViewById<ViewGroup>(android.R.id.content)
    return getView(rootView, AppBarView::class.java)
        ?: throw Exception("NOT AppBarView")
}

inline fun Fragment.getAppBarView (): AppBarView {
    return requireActivity().getAppBarView()
}
