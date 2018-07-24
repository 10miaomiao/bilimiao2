package com.a10miaomiao.bilimiao.utils

import android.content.Context
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
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.AnkoViewDslMarker
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.dip

inline fun ImageView.src(value: String) {
    val url = if ("://" in value) value else "http:$value"
    Glide.with(context)
            .load(url)
            .centerCrop()
//            .transform(GlideRoundTransform(context))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.bili_default_image_tv)
            .dontAnimate()
            .into(this)

}

/**
 * 跳转到另一个Fragment
 */
inline fun MiaoFragment.startFragment(fragment: Fragment) {
//    if (activity is MainActivity){
//        (activity as MainActivity).goto(fragment)
//        return
//    }
    activity!!.supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left)
//            .setCustomAnimations(
//                    R.animator.fragment_slide_left_in, R.animator.fragment_slide_left_out,
//                    R.animator.fragment_slide_right_in, R.animator.fragment_slide_right_out)
            .replace(R.id.mContainer, fragment, null)
            .addToBackStack(null)
            .commit()
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
