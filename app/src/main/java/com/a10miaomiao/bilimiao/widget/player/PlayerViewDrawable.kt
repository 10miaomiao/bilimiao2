package com.a10miaomiao.bilimiao.widget.player

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.view.ViewGroup
import splitties.dimensions.dip

object PlayerViewDrawable {

    fun progressBarDrawable(
        context: Context,
        themeColor: Int
    ): Drawable {
        // 背景层
        val backgroundDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = context.dip(3f)
            setColor(Color.parseColor("#ECF0F1")) // 背景颜色
        }

        // 次要进度层
        val secondaryProgressDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = context.dip(3f)
            setColor(Color.parseColor("#C6CACE")) // 次要进度颜色
        }
        val secondaryProgressClip = ClipDrawable(secondaryProgressDrawable, Gravity.START, ClipDrawable.HORIZONTAL)

        // 主要进度层
        val progressDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = context.dip(3f)
            setColor(themeColor) // 主要进度颜色
        }
        val progressClip = ClipDrawable(progressDrawable, Gravity.START, ClipDrawable.HORIZONTAL)

        return LayerDrawable(
            arrayOf(backgroundDrawable, secondaryProgressClip, progressClip)
        ).apply {
            setId(0, android.R.id.background) // 背景层
            setId(1, android.R.id.secondaryProgress) // 次要进度层
            setId(2, android.R.id.progress) // 主要进度层
        }
    }

    fun bottomProgressBarDrawable(
        context: Context,
        themeColor: Int
    ): Drawable {
        // 背景层
        val backgroundDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = context.dip(1f)
            setColor(Color.parseColor("#4c000000")) // 背景颜色（半透明黑色）
            setSize(ViewGroup.LayoutParams.MATCH_PARENT, context.dip(4))
        }

        // 次要进度层
        val secondaryProgressDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = context.dip(1f)
            setColor(Color.parseColor("#ffe0e0e0")) // 次要进度颜色
            setSize(ViewGroup.LayoutParams.MATCH_PARENT, context.dip(4))
        }
        val secondaryProgressClip = ClipDrawable(secondaryProgressDrawable, Gravity.START, ClipDrawable.HORIZONTAL)

        // 主要进度层
        val progressDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = context.dip(1f)
            setColor(themeColor) // 主要进度颜色（从主题中获取 colorAccent）
            setSize(ViewGroup.LayoutParams.MATCH_PARENT, context.dip(4))
        }
        val progressClip = ClipDrawable(progressDrawable, Gravity.START, ClipDrawable.HORIZONTAL)

        // 创建 LayerDrawable
        return LayerDrawable(arrayOf(backgroundDrawable, secondaryProgressClip, progressClip)).apply {
            setId(0, android.R.id.background) // 背景层
            setId(1, android.R.id.secondaryProgress) // 次要进度层
            setId(2, android.R.id.progress) // 主要进度层
        }
    }
}