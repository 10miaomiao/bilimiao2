package com.a10miaomiao.bilimiao.utils

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.view.ViewManager
import android.widget.ImageView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.MonthPickerView
import com.a10miaomiao.miaoandriod.MiaoFragment
import com.a10miaomiao.miaoandriod.adapter.MiaoRecyclerViewAdapter
import com.a10miaomiao.miaoandriod.adapter.miao
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import me.yokeyword.fragmentation.SupportActivity
import me.yokeyword.fragmentation.SupportFragment
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.AnkoViewDslMarker
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.dip

fun ImageView.network(value: String) {
    val url = if ("://" in value)
        value.replace("http://","https://")
    else
        "https:$value"
    Glide.with(context)
            .load(url)
            .centerCrop()
//            .transform(GlideRoundTransform(context))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.bili_default_image_tv)
            .dontAnimate()
            .into(this)

}

fun Fragment.getStatusBarHeight() = context!!.getStatusBarHeight()
fun View.getStatusBarHeight() = context.getStatusBarHeight()
fun Context.getStatusBarHeight(): Int {
    val activity = this
    if (activity is MainActivity){
        activity.windowInsets?.let {
            return it.systemWindowInsetTop
        }
    }
    var result = 0
    val resourceId = this.resources.getIdentifier("status_bar_height", "dimen",
            "android")
    if (resourceId > 0) {
        result = this.resources.getDimensionPixelSize(resourceId)
    }
    return result
}

/**
 * 跳转到另一个Fragment
 */
inline fun Fragment.startFragment(fragment: SupportFragment) {
    activity?.let { activity ->
        if (activity is SupportActivity) {
            activity.start(fragment)
        }
    }
}

/**
 * 结束当前Fragment
 */
inline fun MiaoFragment.goback() {
    this.activity!!.onBackPressed()
}

inline fun Fragment.attr(resid: Int): Int {
    val typedValue = TypedValue()
    this.context!!.theme.resolveAttribute(resid, typedValue, true)
    return typedValue.resourceId
}

inline fun Context.attr(resid: Int): Int {
    val typedValue = TypedValue()
    this.theme.resolveAttribute(resid, typedValue, true)
    return typedValue.resourceId
}

inline fun View.selectableItemBackground() {
    this.backgroundResource = context.attr(android.R.attr.selectableItemBackground)
}

inline fun View.selectableItemBackgroundBorderless() {
    this.backgroundResource = context.attr(android.R.attr.selectableItemBackgroundBorderless)
}

fun <T : ViewModel> newViewModelFactory(initializer: (() -> T)): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <R : ViewModel?> create(modelClass: Class<R>): R {
            return initializer.invoke() as R
        }
    }
}
