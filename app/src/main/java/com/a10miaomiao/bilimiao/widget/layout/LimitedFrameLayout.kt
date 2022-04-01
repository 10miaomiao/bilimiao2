package com.a10miaomiao.bilimiao.widget.layout

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout

open class LimitedFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var maxWidth = 0
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }

    var maxHeight = 0
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (width > maxWidth && maxWidth > 0) {
            widthMeasureSpec =
                MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.getMode(widthMeasureSpec))
        }
        if (height > maxHeight && maxHeight > 0) {
            heightMeasureSpec =
                MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.getMode(heightMeasureSpec))
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

}