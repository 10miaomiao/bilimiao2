package com.a10miaomiao.bilimiao.ui.commponents

import android.graphics.Color
import android.view.Gravity
import android.view.ViewManager
import android.widget.ImageView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.utils.loadPic
import com.a10miaomiao.bilimiao.utils.selectableItemBackground
import com.a10miaomiao.miaoandriod.ValueManager
import com.bumptech.glide.Glide
import jp.wasabeef.glide.transformations.BlurTransformation
import org.jetbrains.anko.*

fun ViewManager.mediaItemView(
        cover: ValueManager<String?>,
        title: ValueManager<String>,
        subtitle: ValueManager<String>
) {
    frameLayout {
        lparams(matchParent, dip(120))
        padding = dip(5)
        selectableItemBackground()

        imageView {
            //            radius = dip(5)
            applyRecursively(ViewStyle.roundRect(dip(5)))
            scaleType = ImageView.ScaleType.CENTER_CROP
            backgroundColor = 0xFF999999.toInt()
            cover {
                if (it != null && !it.isEmpty()) Glide.with(context)
                            .loadPic(it)
                            .bitmapTransform(BlurTransformation(context, 14, 6)) // 高斯模糊
                            .into(this)
            }
        }

        verticalLayout {
            lparams(matchParent, matchParent)
            gravity = Gravity.CENTER

            textView {
                paint.isFakeBoldText = true
                gravity = Gravity.CENTER
                textSize = 16f
                textColor = Color.WHITE
                title { text = it }
            }
            textView {
                textColor = Color.WHITE
                gravity = Gravity.CENTER
                textSize = 14f
                subtitle { text = it }
            }
        }
    }
}