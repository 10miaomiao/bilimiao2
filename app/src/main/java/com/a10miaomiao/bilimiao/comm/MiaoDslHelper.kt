package com.a10miaomiao.bilimiao.comm

import androidx.fragment.app.Fragment
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import androidx.annotation.IdRes
import androidx.core.widget.NestedScrollView
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.R
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.dsl.core.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun RequestManager.loadImageUrl(value: String): RequestBuilder<Drawable> {
    val url = if ("://" in value)
        value.replace("http://","https://")
    else
        "https:$value"
    return load(url)
}

fun ImageView._netword(
    url: String?
) = miaoEffect(url) {
    if (url != null) {
        Glide.with(context)
            .loadImageUrl(url)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.bili_default_image_tv)
            .dontAnimate()
            .into(this)
    }
}


fun Context.attr(resid: Int): Int {
    val typedValue = TypedValue()
    this.theme.resolveAttribute(resid, typedValue, true)
    return typedValue.resourceId
}