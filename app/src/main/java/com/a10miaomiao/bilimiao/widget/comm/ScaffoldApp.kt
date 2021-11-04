package com.a10miaomiao.bilimiao.widget.comm

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewGroup
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.a10miaomiao.bilimiao.R

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

fun FragmentActivity.getAppBarView (): AppBarView {
    val rootView = findViewById<ViewGroup>(android.R.id.content)
    return getView(rootView, AppBarView::class.java)
        ?: throw Exception("NOT AppBarView")
}
