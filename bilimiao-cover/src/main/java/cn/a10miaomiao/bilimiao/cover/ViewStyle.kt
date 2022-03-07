package cn.a10miaomiao.bilimiao.cover

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

object ViewStyle {

    // 圆角
    fun roundRect(roundCorner: Int) = { v: View ->
        v.clipToOutline = true // 开启裁剪
        v.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height,
                        roundCorner.toFloat())
            }
        }
    }

}