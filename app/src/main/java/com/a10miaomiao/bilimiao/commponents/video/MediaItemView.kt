package com.a10miaomiao.bilimiao.commponents.video

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.comm.MiaoUI
import com.a10miaomiao.bilimiao.comm._network
import com.a10miaomiao.bilimiao.comm.glide.GlideBlurTransformation
import com.a10miaomiao.bilimiao.comm.view.loadPic
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.padding

fun MiaoUI.mediaItemView (
    title: String,
    subtitle: String,
    cover: String? = null,
): View {
    return frameLayout {
        padding = dip(5)
        setBackgroundResource(config.selectableItemBackground)

        views {
            +imageView {
                //            radius = dip(5)
                apply(ViewStyle.roundRect(dip(5)))
                scaleType = ImageView.ScaleType.CENTER_CROP
                backgroundColor = 0xFF999999.toInt()
                miaoEffect(cover) {
                    if (it != null && it.isNotEmpty()) {
                        Glide.with(context)
                            .loadPic(it)
                            .apply(RequestOptions().transform(GlideBlurTransformation(context, 25f)))
                            .into(this)
                    }
                }
            }..lParams(matchParent, dip(100))

            +verticalLayout {

                gravity = Gravity.CENTER

                views {
                    +textView {
                        setTextColor(Color.WHITE)
                        paint.isFakeBoldText = true
                        gravity = Gravity.CENTER
                        textSize = 16f
                        _text = title
                    }..lParams {
                        bottomMargin = dip(5)
                    }
                    +textView {
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER
                        textSize = 14f
                        _text = subtitle
                    }
                }

            }..lParams(matchParent, matchParent)
        }



    }
}