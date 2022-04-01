package com.a10miaomiao.bilimiao.config

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import splitties.views.backgroundColor
import splitties.views.padding

object ViewStyle {
    val block
        get() = { v: View ->
            v.run {
                backgroundColor = config.blockBackgroundColor
                padding = config.dividerSize
            }
        }

    // 圆形
    val circle
        get() = { v: View ->
            v.clipToOutline = true // 开启裁剪
            v.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    val left = 0
                    val top = (view.height - view.width) / 2
                    val right = view.width
                    val bottom = (view.height - view.width) / 2 + view.width
                    outline.setOval(left, top, right, bottom)
                }
            }
        }

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

    fun card(roundCorner: Int, elevation: Float) = { v: View ->
        v.clipToOutline = true // 开启裁剪
        v.elevation = elevation
        v.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height,
                        roundCorner.toFloat())
            }
        }
    }

}