package com.a10miaomiao.bilimiao.comm

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.a10miaomiao.miao.binding.exception.BindingOnlySetException
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import splitties.views.imageDrawable
import splitties.views.imageResource

fun RequestManager.loadImageUrl(
    value: String,
    suffix: String = "",
): RequestBuilder<Drawable> {
    val url = UrlUtil.autoHttps(value)
    return load(url + suffix)
}

fun ImageView.network(
    url: String,
    suffix: String = "",
)  {
    if (url.isBlank()) {
        imageDrawable = null
        return
    }
    Glide.with(context)
        .loadImageUrl(url, suffix)
        .centerCrop()
//            .transform(GlideRoundTransform(context))
        .diskCacheStrategy(DiskCacheStrategy.ALL)
//        .placeholder(R.drawable.bili_default_image_tv)
        .dontAnimate()
        .into(this)
}

fun ImageView._network(
    url: String?,
    suffix: String = "",
) = miaoEffect(url) {
    if (url.isNullOrBlank()) {
        this.imageResource = 0
    } else {
        Glide.with(context)
            .loadImageUrl(url, suffix)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.bili_default_image_tv)
            .dontAnimate()
            .into(this)
    }
}

var SwipeRefreshLayout._isRefreshing: Boolean
    get() { throw BindingOnlySetException() }
    set(value) = miaoEffect(value) {
        isRefreshing = value
    }

fun Context.attr(resid: Int): Int {
    val typedValue = TypedValue()
    this.theme.resolveAttribute(resid, typedValue, true)
    return typedValue.resourceId
}