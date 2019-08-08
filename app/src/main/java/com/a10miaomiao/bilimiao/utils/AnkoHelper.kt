package com.a10miaomiao.bilimiao.utils

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.support.v4.app.Fragment
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.rank.RankCategoryDetailsViewModel
import com.bumptech.glide.DrawableTypeRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import me.yokeyword.fragmentation.SupportActivity
import me.yokeyword.fragmentation.SupportFragment
import org.jetbrains.anko.backgroundResource

fun RequestManager.loadPic(value: String): DrawableTypeRequest<String> {
    val url = if ("://" in value)
        value.replace("http://","https://")
    else
        "https:$value"
    return load(url)
}

fun ImageView.network(url: String) {
    Glide.with(context)
            .loadPic(url)
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

inline fun <reified T : ViewModel> Fragment.getViewModel(): T {
    return ViewModelProviders.of(this).get(T::class.java)
}

inline fun <reified T : ViewModel> Fragment.getViewModel(noinline initializer: (() -> T)): T {
    return ViewModelProviders.of(this, newViewModelFactory(initializer))
            .get(T::class.java)
}




