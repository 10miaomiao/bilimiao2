package com.a10miaomiao.bilimiao.comm.view

import android.graphics.drawable.Drawable
import android.widget.ImageView
import cn.a10miaomiao.miao.binding.exception.BindingOnlySetException
import cn.a10miaomiao.miao.binding.miaoEffect
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import splitties.views.imageDrawable


fun RequestManager.loadPic(value: String): RequestBuilder<Drawable> {
    val url = if ("://" in value)
        value.replace("http://","https://")
    else
        "https:$value"
    return load(url)
}

fun ImageView.network(url: String) = miaoEffect(url) {
    if (url == null || url.isBlank()) {
        imageDrawable = null
        return
    }
    Glide.with(context)
        .loadPic(url)
        .centerCrop()
//            .transform(GlideRoundTransform(context))
        .diskCacheStrategy(DiskCacheStrategy.ALL)
//        .placeholder(R.drawable.bili_default_image_tv)
        .dontAnimate()
        .into(this)
}

inline var ImageView._src: String
    get() { throw BindingOnlySetException() }
    set(value) = network(value)